package com.basitborsa.dto.admin;

import java.time.LocalDateTime;
import java.util.List;

public record SyncResult(
    String status,
    String provider,
    LocalDateTime startedAt,
    LocalDateTime finishedAt,
    int totalRecordsSaved,
    List<SymbolSyncResult> symbols
) {}
