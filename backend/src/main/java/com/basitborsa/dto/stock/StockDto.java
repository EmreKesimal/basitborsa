package com.basitborsa.dto.stock;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockDto(
    Long id,
    String symbol,
    String companyName,
    String sector,
    String description,
    BigDecimal currentPrice,
    BigDecimal dailyChangePercent,
    BigDecimal peRatio,
    BigDecimal pbRatio,
    BigDecimal dividendYield,
    BigDecimal marketCapBillions,
    String dataSource,
    boolean isFallback,
    LocalDateTime lastPriceUpdatedAt,
    String disclaimer
) {}
