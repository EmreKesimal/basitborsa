package com.basitborsa.dto.portfolio;

import java.math.BigDecimal;

public record PortfolioItemDto(
    Long id,
    String symbol,
    String companyName,
    Integer quantity,
    BigDecimal averagePrice,
    BigDecimal currentPrice,
    BigDecimal totalCost,
    BigDecimal currentValue,
    BigDecimal gainLoss,
    BigDecimal gainLossPercent
) {}
