package com.basitborsa.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RelevantNewsRef(
    String title,
    String sourceName,
    String url,
    LocalDate publishedAt,
    String dataSource
) {}
