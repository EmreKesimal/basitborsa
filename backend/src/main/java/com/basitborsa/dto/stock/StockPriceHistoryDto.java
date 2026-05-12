package com.basitborsa.dto.stock;

import java.time.LocalDateTime;
import java.util.List;

public record StockPriceHistoryDto(
    String symbol,
    String dataSource,
    boolean isFallback,
    LocalDateTime lastUpdatedAt,
    List<StockPriceDto> prices,
    String disclaimer
) {}
