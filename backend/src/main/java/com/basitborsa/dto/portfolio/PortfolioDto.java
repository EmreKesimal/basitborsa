package com.basitborsa.dto.portfolio;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioDto(
    Long id,
    BigDecimal virtualBalance,
    BigDecimal totalStockValue,
    BigDecimal totalValue,
    BigDecimal totalGainLoss,
    BigDecimal totalGainLossPercent,
    List<PortfolioItemDto> items,
    String disclaimer
) {}
