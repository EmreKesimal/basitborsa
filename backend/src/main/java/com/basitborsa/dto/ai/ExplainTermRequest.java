package com.basitborsa.dto.ai;

import jakarta.validation.constraints.NotBlank;

public record ExplainTermRequest(
    @NotBlank String term
) {}
