package com.basitborsa.service;

import com.basitborsa.entity.Stock;
import com.basitborsa.entity.StockNews;
import com.basitborsa.exception.ResourceNotFoundException;
import com.basitborsa.repository.StockNewsRepository;
import com.basitborsa.repository.StockRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Read-side news service: serves company + sector news from cache. Always external/KAP only —
 * never returns legacy SEED news (deliberately filtered out).
 */
@Service
@Transactional(readOnly = true)
public class NewsService {

    private static final List<String> ALLOWED_SOURCE_TYPES = List.of("EXTERNAL_NEWS", "CACHED_NEWS", "KAP");

    private final StockRepository stockRepository;
    private final StockNewsRepository stockNewsRepository;
    private final ObjectProvider<NewsSyncService> syncServiceProvider;

    public NewsService(StockRepository stockRepository,
                       StockNewsRepository stockNewsRepository,
                       ObjectProvider<NewsSyncService> syncServiceProvider) {
        this.stockRepository = stockRepository;
        this.stockNewsRepository = stockNewsRepository;
        this.syncServiceProvider = syncServiceProvider;
    }

    public List<StockNews> getRecent(String symbol, int limit) {
        Stock stock = findStock(symbol);
        int cap = limit <= 0 ? 10 : Math.min(limit, 50);
        List<StockNews> rows = stockNewsRepository
                .findBySymbolAndSourceTypeInOrderByPublishedAtDesc(stock.getSymbol(), ALLOWED_SOURCE_TYPES);
        if (rows.isEmpty()) triggerBackgroundSync("recent-empty:" + symbol);
        return rows.stream().limit(cap).toList();
    }

    /**
     * News near a clicked chart date. direction=before returns most recent prior news.
     * direction=after returns next news after that date.
     */
    public List<StockNews> getNear(String symbol, LocalDate date, String direction, int limit) {
        Stock stock = findStock(symbol);
        int cap = limit <= 0 ? 5 : Math.min(limit, 20);
        LocalDate target = date != null ? date : LocalDate.now();
        List<StockNews> rows;
        if ("after".equalsIgnoreCase(direction)) {
            rows = stockNewsRepository.findBySymbolAndPublishedAtGreaterThanEqualOrderByPublishedAtAsc(
                    stock.getSymbol(), target);
        } else {
            rows = stockNewsRepository.findBySymbolAndPublishedAtLessThanEqualOrderByPublishedAtDesc(
                    stock.getSymbol(), target);
        }
        List<StockNews> filtered = rows.stream()
                .filter(n -> n.getSourceType() != null && ALLOWED_SOURCE_TYPES.contains(n.getSourceType()))
                .limit(cap)
                .toList();
        if (filtered.isEmpty()) triggerBackgroundSync("near-empty:" + symbol);
        return filtered;
    }

    /**
     * Sector news prior to a given date — used to enrich AI context when company news is sparse.
     */
    public List<StockNews> getSectorBefore(String sector, LocalDate before, int limit) {
        if (sector == null || sector.isBlank()) return List.of();
        int cap = limit <= 0 ? 3 : Math.min(limit, 10);
        return stockNewsRepository.findBySectorBefore(sector, before, PageRequest.of(0, cap))
                .stream()
                .filter(n -> n.getSourceType() != null && ALLOWED_SOURCE_TYPES.contains(n.getSourceType()))
                .toList();
    }

    /**
     * Dedupe-merged company + sector list, ordered by recency.
     */
    public List<StockNews> getRelevantForAi(String symbol, String sector, LocalDate before, int companyLimit, int sectorLimit) {
        List<StockNews> company = getNear(symbol, before, "before", companyLimit);
        List<StockNews> sectorRows = getSectorBefore(sector, before, sectorLimit);

        LinkedHashMap<String, StockNews> merged = new LinkedHashMap<>();
        Set<String> seenKeys = new LinkedHashSet<>();
        List<StockNews> combined = new ArrayList<>();
        combined.addAll(company);
        combined.addAll(sectorRows);
        for (StockNews n : combined) {
            String key = (n.getSourceUrl() != null && !n.getSourceUrl().isBlank())
                    ? n.getSourceUrl()
                    : (n.getTitle() + "|" + n.getPublishedAt());
            if (seenKeys.add(key)) merged.put(key, n);
        }
        return new ArrayList<>(merged.values());
    }

    /**
     * Pick the most relevant nearby news for the clicked date.
     * Same-day → most-recent-within-14-days-before → soonest-within-3-days-after.
     * Sorted by relevanceScore desc, then by date-distance asc.
     */
    public List<StockNews> findNearestRelevantNews(String symbol, LocalDate clickedDate, int limit) {
        Stock stock = findStock(symbol);
        int cap = limit <= 0 ? 5 : Math.min(limit, 20);
        LocalDate target = clickedDate != null ? clickedDate : LocalDate.now();

        // Same day
        List<StockNews> sameDay = stockNewsRepository
                .findBySymbolAndPublishedAtBetweenAndSourceTypeInOrderByPublishedAtDesc(
                        stock.getSymbol(), target, target, ALLOWED_SOURCE_TYPES);
        if (!sameDay.isEmpty()) return sortByRelevance(sameDay, target, cap);

        // 14 days before
        List<StockNews> before = stockNewsRepository
                .findBySymbolAndPublishedAtBetweenAndSourceTypeInOrderByPublishedAtDesc(
                        stock.getSymbol(), target.minusDays(14), target.minusDays(1), ALLOWED_SOURCE_TYPES);
        if (!before.isEmpty()) return sortByRelevance(before, target, cap);

        // 3 days after
        List<StockNews> after = stockNewsRepository
                .findBySymbolAndPublishedAtBetweenAndSourceTypeInOrderByPublishedAtDesc(
                        stock.getSymbol(), target.plusDays(1), target.plusDays(3), ALLOWED_SOURCE_TYPES);
        if (!after.isEmpty()) return sortByRelevance(after, target, cap);

        triggerBackgroundSync("nearest-empty:" + symbol);
        return List.of();
    }

    private static List<StockNews> sortByRelevance(List<StockNews> rows, LocalDate target, int cap) {
        return rows.stream()
                .sorted(Comparator
                        .comparingInt((StockNews n) -> -score(n))
                        .thenComparingLong(n -> distanceDays(n, target)))
                .limit(cap)
                .toList();
    }

    private static int score(StockNews n) {
        return n.getRelevanceScore() != null ? n.getRelevanceScore() : 0;
    }

    private static long distanceDays(StockNews n, LocalDate target) {
        if (n.getPublishedAt() == null) return Long.MAX_VALUE;
        return Math.abs(n.getPublishedAt().toEpochDay() - target.toEpochDay());
    }

    private void triggerBackgroundSync(String reason) {
        NewsSyncService svc = syncServiceProvider.getIfAvailable();
        if (svc == null) return;
        try { svc.requestAsyncSync(reason); } catch (Exception ignored) {}
    }

    private Stock findStock(String symbol) {
        return stockRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Hisse bulunamadı: " + symbol));
    }
}
