package com.basitborsa.controller;

import com.basitborsa.config.ActiveSymbolsConfig;
import com.basitborsa.dto.admin.SymbolSyncResult;
import com.basitborsa.dto.admin.SyncResult;
import com.basitborsa.entity.DataSyncLog;
import com.basitborsa.repository.DataSyncLogRepository;
import com.basitborsa.service.MarketDataSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/market-data")
public class MarketDataController {

    private static final List<String> MVP_DISABLED_SYMBOLS =
            List.of("ASELS", "BIMAS", "SISE", "TUPRS", "KCHOL", "GARAN", "FROTO");

    private final MarketDataSyncService syncService;
    private final DataSyncLogRepository syncLogRepository;
    private final ActiveSymbolsConfig activeSymbols;

    public MarketDataController(MarketDataSyncService syncService,
                                DataSyncLogRepository syncLogRepository,
                                ActiveSymbolsConfig activeSymbols) {
        this.syncService = syncService;
        this.syncLogRepository = syncLogRepository;
        this.activeSymbols = activeSymbols;
    }

    @PostMapping("/sync-selected")
    public ResponseEntity<Map<String, Object>> syncSelected() {
        SyncResult result = syncService.syncSelectedStocks();
        List<String> active = List.copyOf(activeSymbols.marketSymbols());
        List<String> skipped = MVP_DISABLED_SYMBOLS.stream()
                .filter(s -> !active.contains(s))
                .toList();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", result.status());
        body.put("provider", result.provider());
        body.put("startedAt", result.startedAt());
        body.put("finishedAt", result.finishedAt());
        body.put("totalRecordsSaved", result.totalRecordsSaved());
        body.put("symbols", result.symbols());
        body.put("activeSymbols", active);
        body.put("skippedSymbols", skipped);
        body.put("scopeNote", "Other MVP symbols disabled for hackathon demo scope.");
        return ResponseEntity.ok(body);
    }

    @GetMapping("/sync-status")
    public ResponseEntity<Map<String, Object>> syncStatus() {
        Map<String, SymbolSyncResult> latest = syncService.getLatestSymbolResults();
        List<DataSyncLog> recent = syncLogRepository.findTop10ByOrderByStartedAtDesc();
        return ResponseEntity.ok(Map.of(
            "latestBySymbol", latest,
            "recentRuns", recent
        ));
    }
}
