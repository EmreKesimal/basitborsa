package com.basitborsa.service;

import com.basitborsa.dto.lesson.LessonDto;
import com.basitborsa.exception.ResourceNotFoundException;
import com.basitborsa.mapper.LessonMapper;
import com.basitborsa.repository.LessonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class LessonService {

    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;

    public LessonService(LessonRepository lessonRepository, LessonMapper lessonMapper) {
        this.lessonRepository = lessonRepository;
        this.lessonMapper = lessonMapper;
    }

    public List<LessonDto> getAllLessons() {
        return lessonRepository.findAllByOrderBySortOrderAsc().stream()
                .map(lessonMapper::toDto)
                .toList();
    }

    public LessonDto getLessonBySlug(String slug) {
        return lessonRepository.findBySlug(slug)
                .map(lessonMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Ders bulunamadı: " + slug));
    }
}
