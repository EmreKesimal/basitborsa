package com.basitborsa.service;

import com.basitborsa.entity.Stock;
import com.basitborsa.entity.StockNews;
import com.basitborsa.entity.StockPrice;
import com.basitborsa.repository.StockPriceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Builds the AI chart-story context strictly from real (external/cached) market data and
 * real (external/KAP) news. Returns null when no real market data exists — callers must gate.
 */
@Service
public class AiContextBuilder {

    public static final int MAX_COMPANY_ARTICLES = 5;
    public static final int MAX_SECTOR_ARTICLES = 3;
    public static final int MAX_SNIPPET_CHARS = 280;

    private static final List<String> EXTERNAL_PRICE_SOURCES =
            List.of("EXTERNAL_PROVIDER", "CACHED", "CACHED_EXTERNAL");

    private final StockPriceRepository stockPriceRepository;
    private final NewsService newsService;

    public AiContextBuilder(StockPriceRepository stockPriceRepository, NewsService newsService) {
        this.stockPriceRepository = stockPriceRepository;
        this.newsService = newsService;
    }

    /**
     * Returns null if no real market data is available. Caller MUST treat that as UNAVAILABLE.
     */
    public Map<String, Object> buildChartStoryContext(Stock stock, LocalDate clickedDate) {
        List<StockPrice> realPrices = stockPriceRepository
                .findByStockAndPriceDateAfterAndDataSourceInOrderByPriceDateAsc(
                        stock, LocalDate.now().minusDays(31), EXTERNAL_PRICE_SOURCES);
        if (realPrices.isEmpty()) return null;

        Map<String, Object> priceContext = buildPriceContext(realPrices);
        LocalDate target = clickedDate != null ? clickedDate : LocalDate.now();
        Map<String, Object> nearestPoint = findNearestPricePoint(realPrices, target);
        if (nearestPoint != null) priceContext.put("nearestPoint", nearestPoint);

        List<StockNews> companyNews = newsService.getNear(stock.getSymbol(), target, "before",
                MAX_COMPANY_ARTICLES);
        List<StockNews> sectorNews = newsService.getSectorBefore(stock.getSector(), target,
                MAX_SECTOR_ARTICLES);

        List<Map<String, Object>> companyArticles = companyNews.stream()
                .map(n -> toArticle(n, "COMPANY"))
                .toList();
        List<Map<String, Object>> sectorArticles = sectorNews.stream()
                .filter(n -> companyNews.stream().noneMatch(c -> sameArticle(c, n)))
                .map(n -> toArticle(n, "SECTOR"))
                .toList();

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("symbol", stock.getSymbol());
        ctx.put("companyName", stock.getCompanyName());
        ctx.put("sector", stock.getSector() != null ? stock.getSector() : "");
        ctx.put("priceContext", priceContext);
        ctx.put("clickedDate", target.toString());
        ctx.put("companyArticles", companyArticles);
        ctx.put("sectorArticles", sectorArticles);
        ctx.put("articlesAvailable", !companyArticles.isEmpty() || !sectorArticles.isEmpty());
        ctx.put("userLevel", "beginner");
        ctx.put("language", "tr");
        return ctx;
    }

    private static boolean sameArticle(StockNews a, StockNews b) {
        if (a == null || b == null) return false;
        if (a.getSourceUrl() != null && b.getSourceUrl() != null
                && !a.getSourceUrl().isBlank() && a.getSourceUrl().equals(b.getSourceUrl())) {
            return true;
        }
        return a.getTitle() != null && a.getTitle().equals(b.getTitle())
                && a.getPublishedAt() != null && a.getPublishedAt().equals(b.getPublishedAt());
    }

    private Map<String, Object> toArticle(StockNews n, String relation) {
        Map<String, Object> m = new HashMap<>();
        m.put("title", n.getTitle());
        m.put("snippet", trim(n.getSummary(), MAX_SNIPPET_CHARS));
        m.put("sourceName", n.getSourceName());
        m.put("url", n.getSourceUrl());
        m.put("publishedAt", n.getPublishedAt() != null ? n.getPublishedAt().toString() : null);
        m.put("dataSource", "KAP".equals(n.getSourceType()) ? "KAP" : "EXTERNAL_NEWS");
        m.put("relation", relation);
        return m;
    }

    private static String trim(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0, max - 1)) + "…";
    }

    private Map<String, Object> findNearestPricePoint(List<StockPrice> prices, LocalDate target) {
        Optional<StockPrice> exact = prices.stream()
                .filter(p -> p.getPriceDate() != null && !p.getPriceDate().isAfter(target))
                .max(Comparator.comparing(StockPrice::getPriceDate));
        StockPrice picked = exact.orElseGet(() -> prices.get(prices.size() - 1));
        if (picked == null || picked.getClosePrice() == null) return null;
        Map<String, Object> m = new HashMap<>();
        m.put("date", picked.getPriceDate().toString());
        m.put("close", picked.getClosePrice().doubleValue());
        if (picked.getOpenPrice() != null) m.put("open", picked.getOpenPrice().doubleValue());
        if (picked.getHighPrice() != null) m.put("high", picked.getHighPrice().doubleValue());
        if (picked.getLowPrice() != null)  m.put("low",  picked.getLowPrice().doubleValue());
        if (picked.getVolume() != null)    m.put("volume", picked.getVolume());
        return m;
    }

    private Map<String, Object> buildPriceContext(Collection<StockPrice> prices) {
        Map<String, Object> ctx = new HashMap<>();
        List<StockPrice> sorted = prices.stream()
                .filter(p -> p.getClosePrice() != null)
                .sorted(Comparator.comparing(StockPrice::getPriceDate))
                .toList();
        ctx.put("range", "30d");
        if (sorted.isEmpty()) {
            ctx.put("dataSource", "UNAVAILABLE");
            return ctx;
        }

        BigDecimal latest = sorted.get(sorted.size() - 1).getClosePrice();
        BigDecimal previous = sorted.size() > 1 ? sorted.get(sorted.size() - 2).getClosePrice() : null;
        BigDecimal highest = sorted.stream().map(StockPrice::getClosePrice).max(Comparator.naturalOrder()).orElse(null);
        BigDecimal lowest  = sorted.stream().map(StockPrice::getClosePrice).min(Comparator.naturalOrder()).orElse(null);

        long totalVol = 0; int volCount = 0;
        for (StockPrice p : sorted) {
            if (p.getVolume() != null) { totalVol += p.getVolume(); volCount++; }
        }
        String volumeTrend = "normal";
        if (volCount > 0) {
            long avg = totalVol / volCount;
            Long lastVol = sorted.get(sorted.size() - 1).getVolume();
            if (lastVol != null) {
                if (lastVol > avg * 1.3) volumeTrend = "above_average";
                else if (lastVol < avg * 0.7) volumeTrend = "below_average";
            }
        }

        ctx.put("latestClose", latest.doubleValue());
        if (previous != null) ctx.put("previousClose", previous.doubleValue());
        if (previous != null && previous.compareTo(BigDecimal.ZERO) != 0) {
            double change = latest.subtract(previous)
                    .divide(previous, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
            ctx.put("changePercent", Math.round(change * 100.0) / 100.0);
        }
        if (highest != null) ctx.put("highestPrice", highest.doubleValue());
        if (lowest  != null) ctx.put("lowestPrice",  lowest.doubleValue());
        ctx.put("volumeTrend", volumeTrend);

        String src = sorted.get(sorted.size() - 1).getDataSource();
        ctx.put("dataSource", "EXTERNAL_PROVIDER".equals(src) ? "EXTERNAL_PROVIDER" : "CACHED_EXTERNAL");
        return ctx;
    }

    @SuppressWarnings("unused")
    private List<String> noop(ArrayList<String> ignored) { return List.of(); }
}
