package com.basitborsa.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChartStoryResponse(
    String summary,
    List<ChartStorySection> sections,
    List<String> warnings,
    String sourceType,
    boolean safetyPassed,
    List<RelevantNewsRef> relevantNews,
    List<String> sourcesUsed
) {
    public ChartStoryResponse(String summary, List<ChartStorySection> sections,
                              List<String> warnings, String sourceType, boolean safetyPassed) {
        this(summary, sections, warnings, sourceType, safetyPassed, List.of(), List.of());
    }
}
