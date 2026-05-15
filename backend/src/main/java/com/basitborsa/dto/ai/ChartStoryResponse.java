package com.basitborsa.dto.ai;

import java.util.List;

public record ChartStoryResponse(
    String summary,
    List<ChartStorySection> sections,
    List<String> warnings,
    String sourceType,
    boolean safetyPassed
) {}
