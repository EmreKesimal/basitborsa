package com.basitborsa.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_news",
        indexes = {
                @Index(name = "idx_stock_news_symbol_date", columnList = "symbol, published_at"),
                @Index(name = "idx_stock_news_source_url", columnList = "source_url")
        })
public class StockNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "source_name", length = 100)
    private String sourceName;

    @Column(name = "source_url", length = 1000)
    private String sourceUrl;

    @Column(name = "published_at", nullable = false)
    private LocalDate publishedAt;

    @Column(name = "related_symbols", length = 200)
    private String relatedSymbols;

    @Column(name = "matched_sectors", length = 200)
    private String matchedSectors;

    @Column(length = 50)
    private String category;

    /** Feed origin tag: COMPANY | SECTOR | COMMODITY | MACRO | GLOBAL. */
    @Column(name = "feed_category", length = 20)
    private String feedCategory;

    @Column(length = 8)
    private String language;

    /** EXTERNAL_NEWS | CACHED_NEWS | KAP | SEED (legacy only) */
    @Column(name = "source_type", length = 30)
    private String sourceType = "EXTERNAL_NEWS";

    @Column(name = "relevance_score")
    private Integer relevanceScore;

    @Column(name = "fetched_at")
    private LocalDateTime fetchedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public StockNews() {}

    public StockNews(String symbol, String title, String summary, String sourceName,
                     String sourceUrl, LocalDate publishedAt, String relatedSymbols,
                     String category, String sourceType) {
        this.symbol = symbol;
        this.title = title;
        this.summary = summary;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.publishedAt = publishedAt;
        this.relatedSymbols = relatedSymbols;
        this.category = category;
        this.sourceType = sourceType != null ? sourceType : "EXTERNAL_NEWS";
    }

    public Long getId() { return id; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public LocalDate getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDate publishedAt) { this.publishedAt = publishedAt; }
    public String getRelatedSymbols() { return relatedSymbols; }
    public void setRelatedSymbols(String relatedSymbols) { this.relatedSymbols = relatedSymbols; }
    public String getMatchedSectors() { return matchedSectors; }
    public void setMatchedSectors(String matchedSectors) { this.matchedSectors = matchedSectors; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getFeedCategory() { return feedCategory; }
    public void setFeedCategory(String feedCategory) { this.feedCategory = feedCategory; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public Integer getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(Integer relevanceScore) { this.relevanceScore = relevanceScore; }
    public LocalDateTime getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
