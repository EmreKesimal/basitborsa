package com.basitborsa.repository;

import com.basitborsa.entity.Stock;
import com.basitborsa.entity.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {
    List<StockPrice> findByStockAndPriceDateAfterOrderByPriceDateAsc(Stock stock, LocalDate after);
    Optional<StockPrice> findTopByStockOrderByPriceDateDesc(Stock stock);
    boolean existsByStockAndPriceDate(Stock stock, LocalDate date);
}
