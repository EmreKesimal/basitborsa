package com.basitborsa.repository;

import com.basitborsa.entity.Portfolio;
import com.basitborsa.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    Optional<Portfolio> findByUser(User user);
}
