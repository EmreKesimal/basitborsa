package com.basitborsa.dto.stock;

import java.time.LocalDate;

public record StockNewsDto(
        Long id,
        String symbol,
        String title,
        String summary,
        String sourceName,
        String sourceUrl,
        LocalDate publishedAt,
        String category,
        String sourceType,
        String feedCategory
) {}
