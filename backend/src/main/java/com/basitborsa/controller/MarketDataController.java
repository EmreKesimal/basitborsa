package com.basitborsa.controller;

import com.basitborsa.entity.DataSyncLog;
import com.basitborsa.repository.DataSyncLogRepository;
import com.basitborsa.service.MarketDataSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/market-data")
public class MarketDataController {

    private final MarketDataSyncService syncService;
    private final DataSyncLogRepository syncLogRepository;

    public MarketDataController(MarketDataSyncService syncService, DataSyncLogRepository syncLogRepository) {
        this.syncService = syncService;
        this.syncLogRepository = syncLogRepository;
    }

    @PostMapping("/sync-selected")
    public ResponseEntity<Map<String, String>> syncSelected() {
        syncService.syncSelectedStocks();
        return ResponseEntity.ok(Map.of("status", "Sync started"));
    }

    @GetMapping("/sync-status")
    public ResponseEntity<List<DataSyncLog>> syncStatus() {
        return ResponseEntity.ok(syncLogRepository.findTop10ByOrderByStartedAtDesc());
    }
}
