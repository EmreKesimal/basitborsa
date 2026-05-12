package com.basitborsa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_explanations")
public class AiExplanation {

    public enum ExplanationType { EVENT, TERM, STOCK }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "explanation_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ExplanationType explanationType;

    @Column(name = "cache_key", nullable = false, unique = true)
    private String cacheKey;

    @Column(name = "prompt_summary", columnDefinition = "TEXT")
    private String promptSummary;

    @Column(columnDefinition = "TEXT")
    private String response;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AiExplanation() {}

    public Long getId() { return id; }
    public ExplanationType getExplanationType() { return explanationType; }
    public void setExplanationType(ExplanationType explanationType) { this.explanationType = explanationType; }
    public String getCacheKey() { return cacheKey; }
    public void setCacheKey(String cacheKey) { this.cacheKey = cacheKey; }
    public String getPromptSummary() { return promptSummary; }
    public void setPromptSummary(String promptSummary) { this.promptSummary = promptSummary; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
