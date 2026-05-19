package com.basitborsa.service;

import com.basitborsa.dto.stock.StockDto;
import com.basitborsa.dto.stock.StockEventDto;
import com.basitborsa.dto.stock.StockNewsDto;
import com.basitborsa.dto.stock.StockPriceHistoryDto;
import com.basitborsa.entity.Stock;
import com.basitborsa.entity.StockNews;
import com.basitborsa.entity.StockPrice;
import com.basitborsa.exception.ResourceNotFoundException;
import com.basitborsa.mapper.StockMapper;
import com.basitborsa.repository.StockEventRepository;
import com.basitborsa.repository.StockNewsRepository;
import com.basitborsa.repository.StockPriceRepository;
import com.basitborsa.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.basitborsa.util.AppConstants.*;

@Service
@Transactional(readOnly = true)
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);
    private static final List<String> EXTERNAL_SOURCES = List.of("EXTERNAL_PROVIDER", "CACHED", "CACHED_EXTERNAL");
    private static final String DS_EXTERNAL = "EXTERNAL_PROVIDER";
    private static final String DS_CACHED   = "CACHED_EXTERNAL";
    private static final String DS_UNAVAILABLE = "UNAVAILABLE";

    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final StockEventRepository stockEventRepository;
    private final StockNewsRepository stockNewsRepository;
    private final StockMapper stockMapper;
    // Lazy to avoid circular dep — sync service depends on repos
    private final ObjectProvider<MarketDataSyncService> syncServiceProvider;

    public StockService(StockRepository stockRepository,
                        StockPriceRepository stockPriceRepository,
                        StockEventRepository stockEventRepository,
                        StockNewsRepository stockNewsRepository,
                        StockMapper stockMapper,
                        ObjectProvider<MarketDataSyncService> syncServiceProvider) {
        this.stockRepository = stockRepository;
        this.stockPriceRepository = stockPriceRepository;
        this.stockEventRepository = stockEventRepository;
        this.stockNewsRepository = stockNewsRepository;
        this.stockMapper = stockMapper;
        this.syncServiceProvider = syncServiceProvider;
    }

    public List<StockNewsDto> getNews(String symbol, int limit) {
        String upper = symbol.toUpperCase();
        findBySymbol(upper);
        int cap = limit <= 0 ? 20 : Math.min(limit, 50);
        List<StockNews> rows = stockNewsRepository.findBySymbolOrderByPublishedAtDesc(upper);
        return rows.stream()
                .limit(cap)
                .map(n -> new StockNewsDto(
                        n.getId(), n.getSymbol(), n.getTitle(), n.getSummary(),
                        n.getSourceName(), n.getSourceUrl(), n.getPublishedAt(),
                        n.getCategory(), n.getSourceType()))
                .toList();
    }

    public List<StockDto> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(stockMapper::toDto)
                .toList();
    }

    public StockDto getStock(String symbol) {
        Stock stock = findBySymbol(symbol);
        return stockMapper.toDto(stock);
    }

    public List<StockEventDto> getEvents(String symbol) {
        Stock stock = findBySymbol(symbol);
        return stockEventRepository.findByStockOrderByEventDateAsc(stock).stream()
                .map(stockMapper::toEventDto)
                .toList();
    }

    public StockPriceHistoryDto getPriceHistory(String symbol, int days) {
        Stock stock = findBySymbol(symbol);
        LocalDate cutoff = LocalDate.now().minusDays(days);
        LocalDateTime updatedAt = stock.getLastPriceUpdatedAt();

        var externalPrices = stockPriceRepository
                .findByStockAndPriceDateAfterAndDataSourceInOrderByPriceDateAsc(stock, cutoff, EXTERNAL_SOURCES)
                .stream()
                .filter(this::isValidOhlc)
                .map(stockMapper::toPriceDto)
                .toList();

        if (!externalPrices.isEmpty()) {
            String src = stock.getDataSource() != null && stock.getDataSource().equals(DS_EXTERNAL)
                    ? DS_EXTERNAL : DS_CACHED;
            return new StockPriceHistoryDto(symbol, src, false, updatedAt, externalPrices, DATA_DISCLAIMER);
        }

        // No external/cached data — auto-trigger background sync so demo recovers fast
        triggerBackgroundSync(stock.getSymbol(), "price-cache-miss");
        return new StockPriceHistoryDto(symbol, DS_UNAVAILABLE, false, updatedAt, List.of(),
                "Gerçek piyasa verisi şu an mevcut değil. Sağlayıcı senkronizasyonu deneniyor.");
    }

    private void triggerBackgroundSync(String symbol, String reason) {
        MarketDataSyncService svc = syncServiceProvider.getIfAvailable();
        if (svc == null) {
            log.warn("Sync service unavailable for {} ({}).", symbol, reason);
            return;
        }
        try {
            svc.requestAsyncSync(symbol, reason);
        } catch (Exception e) {
            log.warn("Async sync trigger failed for {}: {}", symbol, e.getMessage());
        }
    }

    private boolean isValidOhlc(StockPrice p) {
        BigDecimal h = p.getHighPrice();
        BigDecimal l = p.getLowPrice();
        BigDecimal c = p.getClosePrice();
        BigDecimal o = p.getOpenPrice();
        if (c == null) return false;
        if (h == null || l == null) return true;
        boolean highGeqLow   = h.compareTo(l) >= 0;
        boolean highGeqClose = h.compareTo(c) >= 0;
        boolean lowLeqClose  = l.compareTo(c) <= 0;
        boolean highGeqOpen  = o == null || h.compareTo(o) >= 0;
        boolean lowLeqOpen   = o == null || l.compareTo(o) <= 0;
        return highGeqLow && highGeqClose && lowLeqClose && highGeqOpen && lowLeqOpen;
    }

    private Stock findBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Hisse bulunamadı: " + symbol));
    }
}
