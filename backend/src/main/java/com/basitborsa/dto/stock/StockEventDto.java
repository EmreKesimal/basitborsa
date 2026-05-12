package com.basitborsa.dto.stock;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StockEventDto(
    Long id,
    LocalDate eventDate,
    String title,
    String eventType,
    BigDecimal priceChangePercent,
    String shortDescription,
    String relatedNews,
    String learningNote
) {}
