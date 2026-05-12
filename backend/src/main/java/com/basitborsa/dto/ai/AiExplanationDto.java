package com.basitborsa.dto.ai;

import java.util.List;

public record AiExplanationDto(
    String summary,
    List<String> possibleFactors,
    String learningNote,
    String disclaimer,
    boolean cached
) {}
