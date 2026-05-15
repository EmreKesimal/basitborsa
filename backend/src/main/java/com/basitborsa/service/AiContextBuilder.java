package com.basitborsa.service;

import com.basitborsa.entity.Stock;
import com.basitborsa.entity.StockEvent;
import com.basitborsa.entity.StockPrice;
import com.basitborsa.repository.StockEventRepository;
import com.basitborsa.repository.StockPriceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiContextBuilder {

    private final StockPriceRepository stockPriceRepository;
    private final StockEventRepository stockEventRepository;

    public AiContextBuilder(StockPriceRepository stockPriceRepository,
                             StockEventRepository stockEventRepository) {
        this.stockPriceRepository = stockPriceRepository;
        this.stockEventRepository = stockEventRepository;
    }

    public Map<String, Object> buildChartStoryContext(Stock stock, StockEvent event) {
        List<StockPrice> prices = stockPriceRepository
                .findByStockAndPriceDateAfterOrderByPriceDateAsc(stock, LocalDate.now().minusDays(31));

        Map<String, Object> priceContext = buildPriceContext(prices);

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("symbol", stock.getSymbol());
        ctx.put("companyName", stock.getCompanyName());
        ctx.put("sector", stock.getSector() != null ? stock.getSector() : "");
        ctx.put("priceContext", priceContext);
        ctx.put("userLevel", "beginner");
        ctx.put("language", "tr");

        if (event != null) {
            Map<String, Object> selectedEvent = new HashMap<>();
            selectedEvent.put("eventDate", event.getEventDate().toString());
            selectedEvent.put("eventTitle", event.getTitle());
            selectedEvent.put("priceChangePercent",
                    event.getPriceChangePercent() != null ? event.getPriceChangePercent().doubleValue() : 0.0);
            List<String> news = event.getRelatedNews() != null
                    ? Arrays.asList(event.getRelatedNews().split(";"))
                    : List.of();
            selectedEvent.put("relatedNews", news);
            selectedEvent.put("learningNote",
                    event.getLearningNote() != null ? event.getLearningNote() : "");
            ctx.put("selectedEvent", selectedEvent);
        }

        return ctx;
    }

    private Map<String, Object> buildPriceContext(List<StockPrice> prices) {
        Map<String, Object> ctx = new HashMap<>();
        if (prices == null || prices.isEmpty()) {
            ctx.put("range", "30d");
            ctx.put("dataSource", "SEED");
            return ctx;
        }

        BigDecimal latest = null;
        BigDecimal previous = null;
        BigDecimal highest = null;
        BigDecimal lowest = null;
        long totalVolume = 0;
        int volumeCount = 0;

        List<StockPrice> sorted = prices.stream()
                .filter(p -> p.getClosePrice() != null)
                .sorted(Comparator.comparing(StockPrice::getPriceDate))
                .toList();

        if (!sorted.isEmpty()) {
            latest = sorted.get(sorted.size() - 1).getClosePrice();
            if (sorted.size() > 1) {
                previous = sorted.get(sorted.size() - 2).getClosePrice();
            }
            highest = sorted.stream()
                    .map(StockPrice::getClosePrice)
                    .filter(p -> p != null)
                    .max(Comparator.naturalOrder())
                    .orElse(null);
            lowest = sorted.stream()
                    .map(StockPrice::getClosePrice)
                    .filter(p -> p != null)
                    .min(Comparator.naturalOrder())
                    .orElse(null);
            for (StockPrice p : sorted) {
                if (p.getVolume() != null) {
                    totalVolume += p.getVolume();
                    volumeCount++;
                }
            }
        }

        ctx.put("range", "30d");
        ctx.put("latestClose", latest != null ? latest.doubleValue() : null);
        ctx.put("previousClose", previous != null ? previous.doubleValue() : null);

        if (latest != null && previous != null && previous.compareTo(BigDecimal.ZERO) != 0) {
            double change = latest.subtract(previous)
                    .divide(previous, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
            ctx.put("changePercent", Math.round(change * 100.0) / 100.0);
        }

        ctx.put("highestPrice", highest != null ? highest.doubleValue() : null);
        ctx.put("lowestPrice", lowest != null ? lowest.doubleValue() : null);

        String volumeTrend = "normal";
        if (volumeCount > 0) {
            long avg = totalVolume / volumeCount;
            StockPrice lastP = sorted.get(sorted.size() - 1);
            if (lastP.getVolume() != null) {
                volumeTrend = lastP.getVolume() > avg * 1.3 ? "above_average"
                        : lastP.getVolume() < avg * 0.7 ? "below_average" : "normal";
            }
        }
        ctx.put("volumeTrend", volumeTrend);

        String dataSource = sorted.isEmpty() ? "SEED"
                : sorted.get(sorted.size() - 1).getDataSource() != null
                        ? sorted.get(sorted.size() - 1).getDataSource()
                        : "SEED";
        ctx.put("dataSource", dataSource);

        return ctx;
    }
}
