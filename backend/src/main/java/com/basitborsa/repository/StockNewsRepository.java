package com.basitborsa.repository;

import com.basitborsa.entity.StockNews;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StockNewsRepository extends JpaRepository<StockNews, Long> {
    List<StockNews> findBySymbolOrderByPublishedAtDesc(String symbol);

    List<StockNews> findBySymbolAndPublishedAtLessThanEqualOrderByPublishedAtDesc(
            String symbol, LocalDate publishedAt);

    List<StockNews> findBySymbolAndPublishedAtGreaterThanEqualOrderByPublishedAtAsc(
            String symbol, LocalDate publishedAt);

    List<StockNews> findBySymbolAndSourceTypeInOrderByPublishedAtDesc(
            String symbol, Collection<String> sourceTypes);

    Optional<StockNews> findFirstBySourceUrl(String sourceUrl);

    boolean existsBySymbolAndTitleAndPublishedAt(String symbol, String title, LocalDate publishedAt);

    @Query("select n from StockNews n where lower(n.matchedSectors) like lower(concat('%', :sector, '%')) " +
            "and n.publishedAt <= :before order by n.publishedAt desc")
    List<StockNews> findBySectorBefore(@Param("sector") String sector,
                                       @Param("before") LocalDate before,
                                       Pageable pageable);

    List<StockNews> findBySymbolAndPublishedAtBetweenAndSourceTypeInOrderByPublishedAtDesc(
            String symbol, LocalDate from, LocalDate to, Collection<String> sourceTypes);
}
