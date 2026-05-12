package com.basitborsa.mapper;

import com.basitborsa.dto.lesson.LessonDto;
import com.basitborsa.entity.Lesson;
import org.springframework.stereotype.Component;

@Component
public class LessonMapper {

    public LessonDto toDto(Lesson lesson) {
        return new LessonDto(
            lesson.getId(),
            lesson.getTitle(),
            lesson.getSlug(),
            lesson.getShortDescription(),
            lesson.getContent(),
            lesson.getExampleText(),
            lesson.getWhyItMatters(),
            lesson.getBeginnerWarning(),
            lesson.getIconName(),
            lesson.getAccentColor(),
            lesson.getDifficultyLevel() != null ? lesson.getDifficultyLevel().name() : "BEGINNER",
            lesson.getSortOrder()
        );
    }
}
