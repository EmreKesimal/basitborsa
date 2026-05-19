package com.basitborsa.service;

import com.basitborsa.dto.stock.StockPriceDto;
import com.basitborsa.entity.DataSyncLog;
import com.basitborsa.entity.Stock;
import com.basitborsa.entity.StockPrice;
import com.basitborsa.provider.MarketDataProvider;
import com.basitborsa.repository.DataSyncLogRepository;
import com.basitborsa.repository.StockPriceRepository;
import com.basitborsa.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class MarketDataSyncService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataSyncService.class);

    private static final List<String> SELECTED_SYMBOLS =
        List.of("THYAO", "ASELS", "BIMAS", "SISE", "TUPRS", "KCHOL", "GARAN", "FROTO");

    private static final long ASYNC_THROTTLE_MS = 60_000L; // 1 min per symbol

    private final List<MarketDataProvider> providers;
    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final DataSyncLogRepository syncLogRepository;
    private final ConcurrentMap<String, Long> lastAsyncSync = new ConcurrentHashMap<>();

    public MarketDataSyncService(List<MarketDataProvider> providers,
                                 StockRepository stockRepository,
                                 StockPriceRepository stockPriceRepository,
                                 DataSyncLogRepository syncLogRepository) {
        this.providers = providers;
        this.stockRepository = stockRepository;
        this.stockPriceRepository = stockPriceRepository;
        this.syncLogRepository = syncLogRepository;
    }

    @Scheduled(cron = "0 0 19 * * MON-FRI", zone = "Europe/Istanbul")
    public void syncDailyPrices() {
        syncSelectedStocks();
    }

    @Async
    public void requestAsyncSync(String symbol, String reason) {
        if (symbol == null) return;
        long now = System.currentTimeMillis();
        Long prev = lastAsyncSync.get(symbol);
        if (prev != null && (now - prev) < ASYNC_THROTTLE_MS) {
            log.debug("Async sync throttled for {} ({})", symbol, reason);
            return;
        }
        lastAsyncSync.put(symbol, now);

        MarketDataProvider provider = providers.stream()
                .filter(MarketDataProvider::isAvailable)
                .findFirst().orElse(null);
        if (provider == null) {
            log.warn("Async sync requested for {} but no provider available ({})", symbol, reason);
            return;
        }
        try {
            int n = syncStock(symbol.toUpperCase(), provider);
            log.info("Async sync done: symbol={} reason={} records={}", symbol, reason, n);
        } catch (Exception e) {
            log.warn("Async sync failed: symbol={} reason={} err={}", symbol, reason, e.getMessage());
        }
    }

    public void syncSelectedStocks() {
        MarketDataProvider activeProvider = providers.stream()
                .filter(MarketDataProvider::isAvailable)
                .findFirst()
                .orElse(null);

        if (activeProvider == null) {
            log.info("No active market data provider. Seed data remains.");
            writeSyncLog("NONE", "DAILY", DataSyncLog.SyncStatus.FAILED, "No provider available", 0);
            return;
        }

        DataSyncLog syncLog = new DataSyncLog();
        syncLog.setProviderName(activeProvider.getProviderName());
        syncLog.setSyncType("DAILY");
        syncLog.setStatus(DataSyncLog.SyncStatus.STARTED);
        syncLog = syncLogRepository.save(syncLog);

        int totalRecords = 0;
        List<String> failed = new ArrayList<>();

        for (String symbol : SELECTED_SYMBOLS) {
            try {
                int saved = syncStock(symbol, activeProvider);
                totalRecords += saved;
                log.info("Synced {}: {} price records", symbol, saved);
            } catch (Exception e) {
                log.error("Sync failed for {}: {}", symbol, e.getMessage());
                failed.add(symbol);
            }
        }

        syncLog.setStatus(failed.size() == SELECTED_SYMBOLS.size()
                ? DataSyncLog.SyncStatus.FAILED : DataSyncLog.SyncStatus.SUCCESS);
        syncLog.setRecordsProcessed(totalRecords);
        syncLog.setFinishedAt(LocalDateTime.now());
        if (!failed.isEmpty()) {
            syncLog.setErrorMessage("Failed symbols: " + String.join(", ", failed));
        }
        syncLogRepository.save(syncLog);

        log.info("Sync done. Provider={}, records={}, failed={}",
                activeProvider.getProviderName(), totalRecords, failed);
    }

    @Transactional
    protected int syncStock(String symbol, MarketDataProvider provider) {
        var stockOpt = stockRepository.findBySymbol(symbol);
        if (stockOpt.isEmpty()) {
            log.debug("Stock {} not in DB, skipping sync", symbol);
            return 0;
        }
        Stock stock = stockOpt.get();

        List<StockPriceDto> prices = provider.getHistoricalPrices(symbol, 30);
        if (prices == null || prices.isEmpty()) {
            log.debug("Provider returned no prices for {}", symbol);
            return 0;
        }

        int fetched = prices.size();
        int saved = 0;
        int updated = 0;

        for (StockPriceDto dto : prices) {
            if (dto.date() == null || dto.close() == null) continue;
            var existing = stockPriceRepository.findByStockAndPriceDate(stock, dto.date());
            boolean isNew = existing.isEmpty();
            StockPrice sp = existing.orElseGet(StockPrice::new);
            sp.setStock(stock);
            sp.setPriceDate(dto.date());
            sp.setOpenPrice(dto.open());
            sp.setClosePrice(dto.close());
            sp.setHighPrice(dto.high());
            sp.setLowPrice(dto.low());
            sp.setVolume(dto.volume());
            sp.setDataSource("EXTERNAL_PROVIDER");
            stockPriceRepository.save(sp);
            if (isNew) saved++; else updated++;
        }

        log.info("syncStock {}: fetched={} saved={} updated(replaced)={}", symbol, fetched, saved, updated);

        // Update stock header from latest price record
        StockPriceDto latest = prices.stream()
                .filter(p -> p.date() != null && p.close() != null)
                .reduce((a, b) -> a.date().isAfter(b.date()) ? a : b)
                .orElse(null);

        if (latest != null) {
            stock.setCurrentPrice(latest.close());
            stock.setDataSource("EXTERNAL_PROVIDER");
            stock.setFallback(false);
            stock.setLastPriceUpdatedAt(LocalDateTime.now());
            stockRepository.save(stock);
        }

        return saved + updated;
    }

    private void writeSyncLog(String provider, String type, DataSyncLog.SyncStatus status,
                              String error, int count) {
        DataSyncLog entry = new DataSyncLog();
        entry.setProviderName(provider);
        entry.setSyncType(type);
        entry.setStatus(status);
        entry.setErrorMessage(error);
        entry.setRecordsProcessed(count);
        entry.setFinishedAt(LocalDateTime.now());
        syncLogRepository.save(entry);
    }
}
