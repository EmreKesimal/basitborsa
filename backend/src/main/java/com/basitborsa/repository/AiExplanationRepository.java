package com.basitborsa.repository;

import com.basitborsa.entity.AiExplanation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AiExplanationRepository extends JpaRepository<AiExplanation, Long> {
    Optional<AiExplanation> findByCacheKey(String cacheKey);
}
