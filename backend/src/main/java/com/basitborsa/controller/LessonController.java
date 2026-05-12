package com.basitborsa.controller;

import com.basitborsa.dto.lesson.LessonDto;
import com.basitborsa.service.LessonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping
    public ResponseEntity<List<LessonDto>> getAllLessons() {
        return ResponseEntity.ok(lessonService.getAllLessons());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<LessonDto> getLesson(@PathVariable String slug) {
        return ResponseEntity.ok(lessonService.getLessonBySlug(slug));
    }
}
