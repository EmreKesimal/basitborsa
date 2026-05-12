package com.basitborsa.service;

import com.basitborsa.entity.DataSyncLog;
import com.basitborsa.provider.MarketDataProvider;
import com.basitborsa.repository.DataSyncLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MarketDataSyncService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataSyncService.class);

    private final List<MarketDataProvider> providers;
    private final DataSyncLogRepository syncLogRepository;

    public MarketDataSyncService(List<MarketDataProvider> providers, DataSyncLogRepository syncLogRepository) {
        this.providers = providers;
        this.syncLogRepository = syncLogRepository;
    }

    @Scheduled(cron = "0 0 19 * * MON-FRI")
    public void syncDailyPrices() {
        syncSelectedStocks();
    }

    public void syncSelectedStocks() {
        MarketDataProvider activeProvider = providers.stream()
                .filter(MarketDataProvider::isAvailable)
                .findFirst()
                .orElse(null);

        if (activeProvider == null) {
            log.info("No active market data provider available. Using seed data.");
            logSync("NONE", "DAILY", DataSyncLog.SyncStatus.FAILED, "No provider available", 0);
            return;
        }

        DataSyncLog syncLog = new DataSyncLog();
        syncLog.setProviderName(activeProvider.getProviderName());
        syncLog.setSyncType("DAILY");
        syncLog.setStatus(DataSyncLog.SyncStatus.STARTED);
        syncLog = syncLogRepository.save(syncLog);

        try {
            log.info("Starting market data sync with provider: {}", activeProvider.getProviderName());
            int count = 0;
            for (String symbol : activeProvider.getSupportedSymbols()) {
                activeProvider.getHistoricalPrices(symbol, 30);
                count++;
            }
            syncLog.setStatus(DataSyncLog.SyncStatus.SUCCESS);
            syncLog.setRecordsProcessed(count);
            syncLog.setFinishedAt(LocalDateTime.now());
            syncLogRepository.save(syncLog);
            log.info("Sync completed: {} symbols processed", count);
        } catch (Exception e) {
            log.error("Sync failed: {}", e.getMessage());
            syncLog.setStatus(DataSyncLog.SyncStatus.FAILED);
            syncLog.setErrorMessage(e.getMessage());
            syncLog.setFinishedAt(LocalDateTime.now());
            syncLogRepository.save(syncLog);
        }
    }

    private void logSync(String provider, String type, DataSyncLog.SyncStatus status, String error, int count) {
        DataSyncLog log = new DataSyncLog();
        log.setProviderName(provider);
        log.setSyncType(type);
        log.setStatus(status);
        log.setErrorMessage(error);
        log.setRecordsProcessed(count);
        log.setFinishedAt(LocalDateTime.now());
        syncLogRepository.save(log);
    }
}
