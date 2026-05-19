package com.basitborsa.controller;

import com.basitborsa.entity.DataSyncLog;
import com.basitborsa.provider.news.NewsProvider;
import com.basitborsa.repository.DataSyncLogRepository;
import com.basitborsa.service.NewsSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/news")
public class NewsAdminController {

    private final NewsSyncService syncService;
    private final DataSyncLogRepository syncLogRepository;
    private final List<NewsProvider> providers;

    public NewsAdminController(NewsSyncService syncService,
                               DataSyncLogRepository syncLogRepository,
                               List<NewsProvider> providers) {
        this.syncService = syncService;
        this.syncLogRepository = syncLogRepository;
        this.providers = providers;
    }

    @PostMapping("/sync-selected")
    public ResponseEntity<NewsSyncService.SyncResult> syncSelected() {
        return ResponseEntity.ok(syncService.syncAll());
    }

    @GetMapping("/sync-status")
    public ResponseEntity<Map<String, Object>> status() {
        List<DataSyncLog> all = syncLogRepository.findTop10ByOrderByStartedAtDesc();
        List<DataSyncLog> newsOnly = all.stream()
                .filter(l -> "NEWS".equalsIgnoreCase(l.getSyncType()))
                .toList();
        List<Map<String, Object>> providerStatuses = providers.stream()
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("providerName", p.getProviderName());
                    m.put("available", p.isAvailable());
                    m.put("companyTagged", p.isCompanyTagged());
                    return m;
                })
                .toList();
        Map<String, Object> body = new HashMap<>();
        body.put("providers", providerStatuses);
        body.put("recentLogs", newsOnly);
        return ResponseEntity.ok(body);
    }
}
