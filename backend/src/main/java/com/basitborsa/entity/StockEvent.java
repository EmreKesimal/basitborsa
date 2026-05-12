package com.basitborsa.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "stock_events")
public class StockEvent {

    public enum EventType { RISE, FALL, NEUTRAL }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "price_change_percent", precision = 8, scale = 4)
    private BigDecimal priceChangePercent;

    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;

    @Column(name = "related_news", columnDefinition = "TEXT")
    private String relatedNews;

    @Column(name = "learning_note", columnDefinition = "TEXT")
    private String learningNote;

    public StockEvent() {}

    public Long getId() { return id; }
    public Stock getStock() { return stock; }
    public void setStock(Stock stock) { this.stock = stock; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    public BigDecimal getPriceChangePercent() { return priceChangePercent; }
    public void setPriceChangePercent(BigDecimal priceChangePercent) { this.priceChangePercent = priceChangePercent; }
    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
    public String getRelatedNews() { return relatedNews; }
    public void setRelatedNews(String relatedNews) { this.relatedNews = relatedNews; }
    public String getLearningNote() { return learningNote; }
    public void setLearningNote(String learningNote) { this.learningNote = learningNote; }
}
