package com.basitborsa.dto.lesson;

public record LessonDto(
    Long id,
    String title,
    String slug,
    String shortDescription,
    String content,
    String exampleText,
    String whyItMatters,
    String beginnerWarning,
    String iconName,
    String accentColor,
    String difficultyLevel,
    Integer sortOrder
) {}
