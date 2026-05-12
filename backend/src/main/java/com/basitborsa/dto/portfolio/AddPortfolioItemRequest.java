package com.basitborsa.dto.portfolio;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddPortfolioItemRequest(
    @NotBlank String symbol,
    @NotNull @Min(1) Integer quantity
) {}
