package com.basitborsa.repository;

import com.basitborsa.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    Optional<Lesson> findBySlug(String slug);
    List<Lesson> findAllByOrderBySortOrderAsc();
}
