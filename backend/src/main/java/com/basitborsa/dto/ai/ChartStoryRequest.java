package com.basitborsa.dto.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record ChartStoryRequest(
    @NotBlank String symbol,
    Long eventId,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date
) {}
