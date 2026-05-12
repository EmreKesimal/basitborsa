package com.basitborsa.repository;

import com.basitborsa.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findBySymbol(String symbol);
    boolean existsBySymbol(String symbol);
}
