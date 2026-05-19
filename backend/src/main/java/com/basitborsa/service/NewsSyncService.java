package com.basitborsa.service;

import com.basitborsa.config.ActiveSymbolsConfig;
import com.basitborsa.entity.DataSyncLog;
import com.basitborsa.entity.Stock;
import com.basitborsa.entity.StockNews;
import com.basitborsa.provider.news.NewsArticle;
import com.basitborsa.provider.news.NewsProvider;
import com.basitborsa.repository.DataSyncLogRepository;
import com.basitborsa.repository.StockNewsRepository;
import com.basitborsa.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static com.basitborsa.service.CompanyAliasRegistry.normalize;

/**
 * Fetches news from configured providers, matches them to known stocks/sectors,
 * dedupes by URL/title+date, and persists to PostgreSQL.
 *
 * Matching rules per feed type:
 *  - COMPANY: requires a THYAO alias hit on title/summary/url.
 *  - SECTOR/COMMODITY/MACRO/GLOBAL: contextual feeds. Keyword match is enough; the article
 *    is stored under THYAO with the corresponding feedCategory so the UI/AI can render it
 *    as "contextual" instead of direct company news.
 */
@Service
public class NewsSyncService {

    private static final Logger log = LoggerFactory.getLogger(NewsSyncService.class);
    private static final long ASYNC_THROTTLE_MS = 5 * 60_000L;

    private static final String THYAO_SYMBOL = "THYAO";

    private static final List<String> AVIATION_KEYWORDS = List.of(
            "havacilik", "hava yolu", "hava yollari", "ucak", "ucus", "yolcu", "yolcu sayisi",
            "turizm", "airline", "airlines", "aviation", "airport"
    );
    private static final List<String> OIL_KEYWORDS = List.of(
            "brent petrol", "petrol fiyatlari", "ham petrol", "akaryakit", "jet yakiti",
            "yakit maliyeti", "fuel price", "oil prices", "crude oil", "brent"
    );
    private static final List<String> MACRO_KEYWORDS = List.of(
            "kuresel piyasalar", "jeopolitik", "fed", "faiz", "dolar", "usdtry", "doviz",
            "enflasyon", "resesyon", "savas", "orta dogu", "global markets"
    );

    private final List<NewsProvider> providers;
    private final StockRepository stockRepository;
    private final StockNewsRepository stockNewsRepository;
    private final DataSyncLogRepository syncLogRepository;
    private final CompanyAliasRegistry aliasRegistry;
    private final ActiveSymbolsConfig activeSymbols;

    @Value("${news.sync.enabled:true}")
    private boolean syncEnabled;

    private final AtomicLong lastAsyncSyncMs = new AtomicLong(0L);

    public NewsSyncService(List<NewsProvider> providers,
                           StockRepository stockRepository,
                           StockNewsRepository stockNewsRepository,
                           DataSyncLogRepository syncLogRepository,
                           CompanyAliasRegistry aliasRegistry,
                           ActiveSymbolsConfig activeSymbols) {
        this.providers = providers;
        this.stockRepository = stockRepository;
        this.stockNewsRepository = stockNewsRepository;
        this.syncLogRepository = syncLogRepository;
        this.aliasRegistry = aliasRegistry;
        this.activeSymbols = activeSymbols;
    }

    /** Configurable fixed-delay scheduled sync (NEWS_SYNC_INTERVAL_MINUTES, default 10). */
    @Scheduled(fixedDelayString = "#{${news.sync.interval-minutes:10} * 60 * 1000}",
            initialDelayString = "#{${news.sync.interval-minutes:10} * 60 * 1000}")
    public void scheduledSync() {
        if (!syncEnabled) {
            log.debug("Scheduled news sync skipped (news.sync.enabled=false)");
            return;
        }
        try { syncAll(); } catch (Exception e) {
            log.warn("Scheduled news sync failed: {}", e.getMessage());
        }
    }

    @Async
    public void requestAsyncSync(String reason) {
        if (!syncEnabled) return;
        long now = System.currentTimeMillis();
        long prev = lastAsyncSyncMs.get();
        if (prev > 0 && (now - prev) < ASYNC_THROTTLE_MS) {
            log.debug("News async sync throttled ({})", reason);
            return;
        }
        lastAsyncSyncMs.set(now);
        try {
            syncAll();
        } catch (Exception e) {
            log.warn("News async sync failed ({}): {}", reason, e.getMessage());
        }
    }

    /** Rich sync result for admin endpoint. */
    public record ProviderStatus(String providerName, String status, int saved, int skipped, String error) {}
    public record SyncResult(
            String status,
            List<ProviderStatus> providers,
            int savedRecords,
            int skippedRecords,
            List<String> failedProviders,
            List<String> syncedSymbols,
            List<String> activeSymbols,
            LocalDateTime finishedAt
    ) {}

    @Transactional
    public SyncResult syncAll() {
        List<String> activeList = new ArrayList<>(activeSymbols.newsSymbols());
        if (!syncEnabled) {
            return new SyncResult("disabled", List.of(), 0, 0, List.of(), List.of(),
                    activeList, LocalDateTime.now());
        }

        List<ProviderStatus> statuses = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        Set<String> syncedSymbols = new LinkedHashSet<>();
        int totalSaved = 0, totalSkipped = 0;

        if (providers.isEmpty()) {
            writeLog("NONE", DataSyncLog.SyncStatus.FAILED, "No news provider configured", 0);
            return new SyncResult("no_providers", statuses, 0, 0, failed, List.of(),
                    activeList, LocalDateTime.now());
        }

        // Restrict matching to active-news symbols for hackathon MVP scope.
        List<Stock> stocks = stockRepository.findAll().stream()
                .filter(s -> activeSymbols.isNewsActive(s.getSymbol()))
                .toList();
        Map<String, List<String>> aliases = aliasRegistry.symbolAliases();
        Map<String, List<String>> sectorKeys = aliasRegistry.sectorKeywords();

        for (NewsProvider p : providers) {
            if (!p.isAvailable()) {
                statuses.add(new ProviderStatus(p.getProviderName(), "disabled", 0, 0, null));
                continue;
            }
            DataSyncLog logRow = new DataSyncLog();
            logRow.setProviderName(p.getProviderName());
            logRow.setSyncType("NEWS");
            logRow.setStatus(DataSyncLog.SyncStatus.STARTED);
            logRow = syncLogRepository.save(logRow);

            int savedHere = 0, skippedHere = 0;
            String err = null;
            try {
                List<NewsArticle> raw = p.fetchLatest();
                for (NewsArticle a : raw) {
                    PersistOutcome out = persistMatches(a, p, stocks, aliases, sectorKeys, syncedSymbols);
                    savedHere += out.saved;
                    skippedHere += out.skipped;
                }
                logRow.setStatus(DataSyncLog.SyncStatus.SUCCESS);
            } catch (Exception e) {
                err = e.getMessage();
                logRow.setStatus(DataSyncLog.SyncStatus.FAILED);
                logRow.setErrorMessage(err);
                failed.add(p.getProviderName());
                log.warn("News provider {} failed: {}", p.getProviderName(), err);
            }
            logRow.setRecordsProcessed(savedHere);
            logRow.setFinishedAt(LocalDateTime.now());
            syncLogRepository.save(logRow);

            statuses.add(new ProviderStatus(
                    p.getProviderName(),
                    err == null ? "ok" : "error",
                    savedHere, skippedHere, err));
            totalSaved += savedHere;
            totalSkipped += skippedHere;
            log.info("News sync provider={} saved={} skipped={}",
                    p.getProviderName(), savedHere, skippedHere);
        }
        return new SyncResult(
                "ok", statuses, totalSaved, totalSkipped, failed,
                new ArrayList<>(syncedSymbols), activeList, LocalDateTime.now());
    }

    private record PersistOutcome(int saved, int skipped) {}

    private PersistOutcome persistMatches(NewsArticle a, NewsProvider provider,
                                          List<Stock> stocks,
                                          Map<String, List<String>> aliases,
                                          Map<String, List<String>> sectorKeys,
                                          Set<String> syncedSymbolsAcc) {
        if (a == null || a.title() == null || a.publishedAt() == null) return new PersistOutcome(0, 1);

        // Dedupe by URL first
        if (a.url() != null && !a.url().isBlank()) {
            if (stockNewsRepository.findFirstBySourceUrl(a.url()).isPresent()) return new PersistOutcome(0, 1);
        }

        String titleNorm = normalize(a.title());
        String summaryNorm = normalize(a.summary());
        String urlNorm = normalize(a.url());
        String haystack = titleNorm + " " + summaryNorm + " " + urlNorm;

        String feedType = resolveFeedType(a, provider);

        // KAP feeds are always treated as COMPANY-tagged (strict alias gate).
        boolean strictCompany = "COMPANY".equals(feedType);

        // Company alias scoring for THYAO + (legacy) other active stocks.
        LinkedHashMap<String, Integer> symbolScores = new LinkedHashMap<>();
        Set<String> matchedSectors = new LinkedHashSet<>();

        for (Stock s : stocks) {
            String sym = s.getSymbol();
            List<String> al = aliases.getOrDefault(sym, List.of(sym.toLowerCase()));
            int score = 0;
            for (String alias : al) {
                if (alias == null || alias.isBlank()) continue;
                String aliasNorm = normalize(alias.trim());
                if (aliasNorm.isBlank()) continue;
                if (titleNorm.contains(aliasNorm)) score += 10;
                else if (summaryNorm.contains(aliasNorm)) score += 4;
            }
            if (score > 0) {
                symbolScores.put(sym, score);
                if (s.getSector() != null) matchedSectors.add(s.getSector());
            }
        }

        for (var e : sectorKeys.entrySet()) {
            for (String kw : e.getValue()) {
                if (kw == null || kw.isBlank()) continue;
                String kwNorm = normalize(kw);
                if (!kwNorm.isBlank() && haystack.contains(kwNorm)) {
                    matchedSectors.add(e.getKey());
                    break;
                }
            }
        }

        if (strictCompany) {
            if (symbolScores.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("News skipped feed={} type={} title=\"{}\" reason=no_company_alias_match",
                            a.sourceName(), feedType, safeTitle(a));
                }
                return new PersistOutcome(0, 1);
            }
            return persistPerSymbol(a, provider, feedType, symbolScores, matchedSectors,
                    syncedSymbolsAcc);
        }

        // Contextual feed: keyword match against the feed's domain. No company alias required.
        int contextualScore = scoreContextualFeed(feedType, haystack);
        if (contextualScore == 0) {
            if (log.isDebugEnabled()) {
                log.debug("News skipped feed={} type={} title=\"{}\" reason=no_keyword_match",
                        a.sourceName(), feedType, safeTitle(a));
            }
            return new PersistOutcome(0, 1);
        }

        // Persist contextual article under THYAO with the proper feedCategory.
        LinkedHashMap<String, Integer> ctxScores = new LinkedHashMap<>();
        ctxScores.put(THYAO_SYMBOL, contextualScore);
        return persistPerSymbol(a, provider, feedType, ctxScores, matchedSectors, syncedSymbolsAcc);
    }

    private PersistOutcome persistPerSymbol(NewsArticle a, NewsProvider provider, String feedType,
                                            LinkedHashMap<String, Integer> symbolScores,
                                            Set<String> matchedSectors,
                                            Set<String> syncedSymbolsAcc) {
        int saved = 0, skipped = 0;
        LocalDateTime now = LocalDateTime.now();
        for (var entry : symbolScores.entrySet()) {
            String sym = entry.getKey();
            int score = entry.getValue() + (matchedSectors.isEmpty() ? 0 : 1);
            String truncatedTitle = truncate(a.title(), 500);
            if (stockNewsRepository.existsBySymbolAndTitleAndPublishedAt(sym, truncatedTitle, a.publishedAt())) {
                skipped++;
                continue;
            }
            StockNews n = new StockNews();
            n.setSymbol(sym);
            n.setTitle(truncatedTitle);
            n.setSummary(truncate(a.summary(), 4000));
            n.setSourceName(a.sourceName());
            n.setSourceUrl(a.url());
            n.setPublishedAt(a.publishedAt());
            n.setRelatedSymbols(String.join(",", symbolScores.keySet()));
            n.setMatchedSectors(String.join(",", matchedSectors));
            n.setCategory(a.rawCategory());
            n.setFeedCategory(feedType);
            n.setLanguage(a.language());
            n.setSourceType(provider.isCompanyTagged() ? "KAP" : "EXTERNAL_NEWS");
            n.setRelevanceScore(score);
            n.setFetchedAt(now);
            stockNewsRepository.save(n);
            syncedSymbolsAcc.add(sym);
            saved++;
        }
        return new PersistOutcome(saved, skipped);
    }

    /** Resolve the effective feedType for an article — provider overrides win for company-tagged feeds. */
    private static String resolveFeedType(NewsArticle a, NewsProvider provider) {
        if (provider != null && provider.isCompanyTagged()) return "COMPANY";
        String fromArticle = a.feedType();
        if (fromArticle == null || fromArticle.isBlank()) return "COMPANY";
        return switch (fromArticle.toUpperCase()) {
            case "SECTOR" -> "SECTOR";
            case "COMMODITY" -> "COMMODITY";
            case "MACRO" -> "MACRO";
            case "GLOBAL" -> "GLOBAL";
            default -> "COMPANY";
        };
    }

    /** Returns base relevance score for a contextual feed match, or 0 if no keyword hits. */
    private static int scoreContextualFeed(String feedType, String haystack) {
        return switch (feedType) {
            case "SECTOR" -> anyKeyword(haystack, AVIATION_KEYWORDS) ? 6 : 0;
            case "COMMODITY" -> anyKeyword(haystack, OIL_KEYWORDS) ? 5 : 0;
            case "MACRO", "GLOBAL" -> anyKeyword(haystack, MACRO_KEYWORDS) ? 4 : 0;
            default -> 0;
        };
    }

    private static boolean anyKeyword(String haystack, List<String> keywords) {
        if (haystack == null || haystack.isBlank()) return false;
        for (String kw : keywords) {
            if (kw == null || kw.isBlank()) continue;
            String kwNorm = normalize(kw);
            if (!kwNorm.isBlank() && haystack.contains(kwNorm)) return true;
        }
        return false;
    }

    private static String safeTitle(NewsArticle a) {
        if (a == null || a.title() == null) return "";
        String t = a.title();
        return t.length() > 80 ? t.substring(0, 80) + "…" : t;
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    private void writeLog(String provider, DataSyncLog.SyncStatus status, String error, int count) {
        DataSyncLog log = new DataSyncLog();
        log.setProviderName(provider);
        log.setSyncType("NEWS");
        log.setStatus(status);
        log.setErrorMessage(error);
        log.setRecordsProcessed(count);
        log.setFinishedAt(LocalDateTime.now());
        syncLogRepository.save(log);
    }

    @SuppressWarnings("unused")
    private List<NewsArticle> emptyOnError(Exception e, String tag) {
        log.warn("News fetch error {}: {}", tag, e.getMessage());
        return new ArrayList<>();
    }
}
