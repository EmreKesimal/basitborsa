package com.basitborsa.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String symbol;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(length = 100)
    private String sector;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "current_price", precision = 12, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "daily_change_percent", precision = 8, scale = 4)
    private BigDecimal dailyChangePercent;

    @Column(name = "pe_ratio", precision = 8, scale = 2)
    private BigDecimal peRatio;

    @Column(name = "pb_ratio", precision = 8, scale = 2)
    private BigDecimal pbRatio;

    @Column(name = "dividend_yield", precision = 8, scale = 4)
    private BigDecimal dividendYield;

    @Column(name = "market_cap_billions", precision = 12, scale = 2)
    private BigDecimal marketCapBillions;

    @Column(name = "data_source", length = 50)
    private String dataSource;

    @Column(name = "is_fallback")
    private boolean isFallback = false;

    @Column(name = "last_price_updated_at")
    private LocalDateTime lastPriceUpdatedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Stock() {}

    public Long getId() { return id; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    public BigDecimal getDailyChangePercent() { return dailyChangePercent; }
    public void setDailyChangePercent(BigDecimal dailyChangePercent) { this.dailyChangePercent = dailyChangePercent; }
    public BigDecimal getPeRatio() { return peRatio; }
    public void setPeRatio(BigDecimal peRatio) { this.peRatio = peRatio; }
    public BigDecimal getPbRatio() { return pbRatio; }
    public void setPbRatio(BigDecimal pbRatio) { this.pbRatio = pbRatio; }
    public BigDecimal getDividendYield() { return dividendYield; }
    public void setDividendYield(BigDecimal dividendYield) { this.dividendYield = dividendYield; }
    public BigDecimal getMarketCapBillions() { return marketCapBillions; }
    public void setMarketCapBillions(BigDecimal marketCapBillions) { this.marketCapBillions = marketCapBillions; }
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    public boolean isFallback() { return isFallback; }
    public void setFallback(boolean fallback) { isFallback = fallback; }
    public LocalDateTime getLastPriceUpdatedAt() { return lastPriceUpdatedAt; }
    public void setLastPriceUpdatedAt(LocalDateTime lastPriceUpdatedAt) { this.lastPriceUpdatedAt = lastPriceUpdatedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
