package com.basitborsa.service;

import com.basitborsa.config.ActiveSymbolsConfig;
import com.basitborsa.dto.admin.SymbolSyncResult;
import com.basitborsa.dto.admin.SyncResult;
import com.basitborsa.dto.stock.StockPriceDto;
import com.basitborsa.entity.DataSyncLog;
import com.basitborsa.entity.Stock;
import com.basitborsa.entity.StockPrice;
import com.basitborsa.provider.MarketDataProvider;
import com.basitborsa.provider.ProviderFetchResult;
import com.basitborsa.repository.DataSyncLogRepository;
import com.basitborsa.repository.StockPriceRepository;
import com.basitborsa.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class MarketDataSyncService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataSyncService.class);

    private static final long ASYNC_THROTTLE_MS = 60_000L;

    private final List<MarketDataProvider> providers;
    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final DataSyncLogRepository syncLogRepository;
    private final ActiveSymbolsConfig activeSymbols;
    private final ConcurrentMap<String, Long> lastAsyncSync = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, SymbolSyncResult> latestSymbolResults = new ConcurrentHashMap<>();

    @Value("${market.data.sync.delay-ms:8000}")
    private long perSymbolDelayMs;

    public MarketDataSyncService(List<MarketDataProvider> providers,
                                 StockRepository stockRepository,
                                 StockPriceRepository stockPriceRepository,
                                 DataSyncLogRepository syncLogRepository,
                                 ActiveSymbolsConfig activeSymbols) {
        this.providers = providers;
        this.stockRepository = stockRepository;
        this.stockPriceRepository = stockPriceRepository;
        this.syncLogRepository = syncLogRepository;
        this.activeSymbols = activeSymbols;
    }

    @Scheduled(cron = "0 0 19 * * MON-FRI", zone = "Europe/Istanbul")
    public void syncDailyPrices() {
        syncSelectedStocks();
    }

    public Map<String, SymbolSyncResult> getLatestSymbolResults() {
        return Map.copyOf(latestSymbolResults);
    }

    @Async
    public void requestAsyncSync(String symbol, String reason) {
        if (symbol == null) return;
        if (!activeSymbols.isMarketActive(symbol)) {
            log.debug("Async sync skipped for non-active symbol {} ({})", symbol, reason);
            return;
        }
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
            SymbolSyncResult result = syncStock(symbol.toUpperCase(), provider);
            latestSymbolResults.put(result.symbol(), result);
            log.info("Async sync done: symbol={} reason={} status={} records={}",
                    symbol, reason, result.status(), result.recordsSaved());
        } catch (Exception e) {
            log.warn("Async sync failed: symbol={} reason={} err={}", symbol, reason, e.getMessage());
        }
    }

    public SyncResult syncSelectedStocks() {
        LocalDateTime startedAt = LocalDateTime.now();

        MarketDataProvider activeProvider = providers.stream()
                .filter(MarketDataProvider::isAvailable)
                .findFirst()
                .orElse(null);

        if (activeProvider == null) {
            log.info("No active market data provider. Seed data remains.");
            writeSyncLog("NONE", "DAILY", DataSyncLog.SyncStatus.FAILED, "No provider available", 0);
            return new SyncResult("FAILED", "NONE", startedAt, LocalDateTime.now(), 0, List.of());
        }

        List<String> symbols = new ArrayList<>(activeSymbols.marketSymbols());
        if (symbols.isEmpty()) {
            log.warn("No active market symbols configured; sync produced nothing.");
            writeSyncLog(activeProvider.getProviderName(), "DAILY",
                    DataSyncLog.SyncStatus.SUCCESS, "no active symbols configured", 0);
            return new SyncResult("SUCCESS", activeProvider.getProviderName(),
                    startedAt, LocalDateTime.now(), 0, List.of());
        }

        DataSyncLog syncLog = new DataSyncLog();
        syncLog.setProviderName(activeProvider.getProviderName());
        syncLog.setSyncType("DAILY");
        syncLog.setStatus(DataSyncLog.SyncStatus.STARTED);
        syncLog = syncLogRepository.save(syncLog);

        int totalRecords = 0;
        List<SymbolSyncResult> perSymbol = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (int i = 0; i < symbols.size(); i++) {
            String symbol = symbols.get(i);
            SymbolSyncResult result;
            try {
                result = syncStock(symbol, activeProvider);
            } catch (Exception e) {
                log.error("Sync threw for {}: {}", symbol, e.getMessage());
                result = new SymbolSyncResult(
                    symbol,
                    activeProvider.mapToProviderSymbol(symbol),
                    "ERROR",
                    0,
                    null,
                    "exception: " + e.getMessage(),
                    LocalDateTime.now()
                );
            }
            perSymbol.add(result);
            latestSymbolResults.put(result.symbol(), result);
            totalRecords += result.recordsSaved();
            if (!"SUCCESS".equals(result.status())) {
                failed.add(symbol + "=" + result.status());
            }
            log.info("Synced {}: status={} records={} err={}",
                    symbol, result.status(), result.recordsSaved(), result.errorMessage());

            if (i < symbols.size() - 1 && perSymbolDelayMs > 0) {
                try { Thread.sleep(perSymbolDelayMs); }
                catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
            }
        }

        boolean allFailed = failed.size() == symbols.size();
        syncLog.setStatus(allFailed ? DataSyncLog.SyncStatus.FAILED : DataSyncLog.SyncStatus.SUCCESS);
        syncLog.setRecordsProcessed(totalRecords);
        syncLog.setFinishedAt(LocalDateTime.now());
        if (!failed.isEmpty()) {
            syncLog.setErrorMessage("Failed symbols: " + String.join(", ", failed));
        }
        syncLogRepository.save(syncLog);

        log.info("Sync done. provider={} records={} failed={}",
                activeProvider.getProviderName(), totalRecords, failed);

        return new SyncResult(
            allFailed ? "FAILED" : (failed.isEmpty() ? "SUCCESS" : "PARTIAL"),
            activeProvider.getProviderName(),
            startedAt,
            LocalDateTime.now(),
            totalRecords,
            perSymbol
        );
    }

    @Transactional
    protected SymbolSyncResult syncStock(String symbol, MarketDataProvider provider) {
        String providerSymbol = provider.mapToProviderSymbol(symbol);
        LocalDateTime now = LocalDateTime.now();

        var stockOpt = stockRepository.findBySymbol(symbol);
        if (stockOpt.isEmpty()) {
            log.debug("Stock {} not in DB, skipping sync", symbol);
            return new SymbolSyncResult(symbol, providerSymbol, "SKIPPED", 0, null,
                "stock not found in DB", now);
        }
        Stock stock = stockOpt.get();

        ProviderFetchResult fetch = provider.getHistoricalPricesDetailed(symbol, 30);

        switch (fetch.status()) {
            case RATE_LIMIT:
                return new SymbolSyncResult(symbol, providerSymbol, "RATE_LIMITED", 0, null,
                    "rate limit / quota: " + fetch.message(), LocalDateTime.now());
            case ERROR:
                return new SymbolSyncResult(symbol, providerSymbol, "ERROR", 0, null,
                    "provider error: http=" + fetch.httpStatus() + " msg=" + fetch.message(),
                    LocalDateTime.now());
            case UNSUPPORTED:
                return new SymbolSyncResult(symbol, providerSymbol, "UNSUPPORTED", 0, null,
                    fetch.message(), LocalDateTime.now());
            case EMPTY:
                return new SymbolSyncResult(symbol, providerSymbol, "EMPTY", 0, null,
                    "empty provider response", LocalDateTime.now());
            case OK:
            default:
                break;
        }

        List<StockPriceDto> prices = fetch.prices();
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

        log.info("syncStock {}: providerSymbol={} fetched={} saved={} updated={}",
                symbol, providerSymbol, fetched, saved, updated);

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

        return new SymbolSyncResult(
            symbol,
            providerSymbol,
            "SUCCESS",
            saved + updated,
            latest != null ? latest.close() : null,
            null,
            LocalDateTime.now()
        );
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
