package com.basitborsa.dto.stock;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StockPriceDto(
    LocalDate date,
    BigDecimal open,
    BigDecimal high,
    BigDecimal low,
    BigDecimal close,
    Long volume
) {}
