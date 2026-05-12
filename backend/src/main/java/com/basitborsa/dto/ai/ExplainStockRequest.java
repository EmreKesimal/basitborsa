package com.basitborsa.dto.ai;

import jakarta.validation.constraints.NotBlank;

public record ExplainStockRequest(
    @NotBlank String symbol,
    @NotBlank String companyName,
    String sector,
    String question
) {}
