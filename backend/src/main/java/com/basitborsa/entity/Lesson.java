package com.basitborsa.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "lessons")
public class Lesson {

    public enum DifficultyLevel { BEGINNER, INTERMEDIATE, ADVANCED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "example_text", columnDefinition = "TEXT")
    private String exampleText;

    @Column(name = "why_it_matters", columnDefinition = "TEXT")
    private String whyItMatters;

    @Column(name = "beginner_warning", columnDefinition = "TEXT")
    private String beginnerWarning;

    @Column(name = "icon_name", length = 50)
    private String iconName;

    @Column(name = "accent_color", length = 50)
    private String accentColor;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private DifficultyLevel difficultyLevel = DifficultyLevel.BEGINNER;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    public Lesson() {}

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getExampleText() { return exampleText; }
    public void setExampleText(String exampleText) { this.exampleText = exampleText; }
    public String getWhyItMatters() { return whyItMatters; }
    public void setWhyItMatters(String whyItMatters) { this.whyItMatters = whyItMatters; }
    public String getBeginnerWarning() { return beginnerWarning; }
    public void setBeginnerWarning(String beginnerWarning) { this.beginnerWarning = beginnerWarning; }
    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }
    public String getAccentColor() { return accentColor; }
    public void setAccentColor(String accentColor) { this.accentColor = accentColor; }
    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
