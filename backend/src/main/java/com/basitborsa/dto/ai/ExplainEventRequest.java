package com.basitborsa.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExplainEventRequest(
    @NotBlank String symbol,
    @NotBlank String companyName,
    @NotNull LocalDate eventDate,
    BigDecimal priceChangePercent,
    @NotBlank String eventTitle,
    List<String> relatedNews
) {}
