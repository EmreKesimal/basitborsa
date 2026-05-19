package com.basitborsa.provider.news;

import java.util.List;

/**
 * External news source abstraction. Implementations fetch raw articles only;
 * matching, dedupe, persistence is the caller's responsibility.
 */
public interface NewsProvider {

    /** Stable identifier used for logging + dataSource labelling. */
    String getProviderName();

    /** Whether this provider is configured and ready to fetch. */
    boolean isAvailable();

    /** Whether the provider returns company-tagged feeds (KAP) vs. generic news. */
    default boolean isCompanyTagged() { return false; }

    /**
     * Fetch a snapshot of latest articles available from this source.
     * Implementations should respect their configured timeouts and never throw — return empty on error.
     */
    List<NewsArticle> fetchLatest();
}
