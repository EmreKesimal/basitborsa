package com.basitborsa.provider.news;

import java.time.LocalDate;

/**
 * Provider-neutral news article. Matching/persistence is done by NewsService.
 * feedType marks the source intent: COMPANY | SECTOR | COMMODITY | MACRO | GLOBAL.
 */
public record NewsArticle(
        String title,
        String summary,
        String url,
        LocalDate publishedAt,
        String sourceName,
        String language,
        String rawCategory,
        String feedType
) {
    public NewsArticle(String title, String summary, String url, LocalDate publishedAt,
                       String sourceName, String language, String rawCategory) {
        this(title, summary, url, publishedAt, sourceName, language, rawCategory, "COMPANY");
    }
}
