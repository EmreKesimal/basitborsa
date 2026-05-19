package com.basitborsa.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SymbolSyncResult(
    String symbol,
    String providerSymbol,
    String status,
    int recordsSaved,
    BigDecimal latestClose,
    String errorMessage,
    LocalDateTime finishedAt
) {}
