package com.basitborsa.repository;

import com.basitborsa.entity.Stock;
import com.basitborsa.entity.StockEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockEventRepository extends JpaRepository<StockEvent, Long> {
    List<StockEvent> findByStockOrderByEventDateAsc(Stock stock);
}
