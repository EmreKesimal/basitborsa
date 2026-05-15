package com.basitborsa.dto.ai;

import jakarta.validation.constraints.NotBlank;

public record ChartStoryRequest(
    @NotBlank String symbol,
    Long eventId
) {}
