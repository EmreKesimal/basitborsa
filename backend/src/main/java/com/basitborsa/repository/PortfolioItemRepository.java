package com.basitborsa.repository;

import com.basitborsa.entity.Portfolio;
import com.basitborsa.entity.PortfolioItem;
import com.basitborsa.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {
    Optional<PortfolioItem> findByPortfolioAndStock(Portfolio portfolio, Stock stock);
}
