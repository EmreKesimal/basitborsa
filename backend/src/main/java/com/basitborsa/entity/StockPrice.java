package com.basitborsa.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_prices", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"stock_id", "price_date"})
})
public class StockPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;

    @Column(name = "open_price", precision = 12, scale = 2)
    private BigDecimal openPrice;

    @Column(name = "close_price", precision = 12, scale = 2)
    private BigDecimal closePrice;

    @Column(name = "high_price", precision = 12, scale = 2)
    private BigDecimal highPrice;

    @Column(name = "low_price", precision = 12, scale = 2)
    private BigDecimal lowPrice;

    @Column
    private Long volume;

    @Column(name = "data_source", length = 50)
    private String dataSource = "SEED";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public StockPrice() {}

    public Long getId() { return id; }
    public Stock getStock() { return stock; }
    public void setStock(Stock stock) { this.stock = stock; }
    public LocalDate getPriceDate() { return priceDate; }
    public void setPriceDate(LocalDate priceDate) { this.priceDate = priceDate; }
    public BigDecimal getOpenPrice() { return openPrice; }
    public void setOpenPrice(BigDecimal openPrice) { this.openPrice = openPrice; }
    public BigDecimal getClosePrice() { return closePrice; }
    public void setClosePrice(BigDecimal closePrice) { this.closePrice = closePrice; }
    public BigDecimal getHighPrice() { return highPrice; }
    public void setHighPrice(BigDecimal highPrice) { this.highPrice = highPrice; }
    public BigDecimal getLowPrice() { return lowPrice; }
    public void setLowPrice(BigDecimal lowPrice) { this.lowPrice = lowPrice; }
    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
