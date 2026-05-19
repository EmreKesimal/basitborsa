package com.basitborsa.provider.news;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;

/**
 * KAP (Public Disclosure Platform) RSS provider. URL is configurable via NEWS_KAP_RSS_URL.
 * If not configured, the provider stays unavailable and is skipped silently.
 */
@Component
public class KapNewsProvider implements NewsProvider {

    private static final Logger log = LoggerFactory.getLogger(KapNewsProvider.class);

    private final WebClient webClient = WebClient.builder().build();

    @Value("${news.kap.enabled:false}")
    private boolean kapEnabled;

    @Value("${news.kap.rss-url:}")
    private String rssUrl;

    @Value("${news.fetch-timeout-seconds:10}")
    private long timeoutSeconds;

    @Value("${news.http.user-agent:BasitBorsaBot/1.0}")
    private String userAgent;

    @Override public String getProviderName() { return "KAP_RSS"; }

    @Override public boolean isAvailable() {
        return kapEnabled && rssUrl != null && !rssUrl.isBlank();
    }

    @Override public boolean isCompanyTagged() { return true; }

    @Override
    public List<NewsArticle> fetchLatest() {
        if (!isAvailable()) {
            log.info("KAP provider skipped (enabled={}, url blank={})",
                    kapEnabled, rssUrl == null || rssUrl.isBlank());
            return List.of();
        }
        try {
            byte[] bytes = webClient.get()
                    .uri(rssUrl)
                    .header(HttpHeaders.USER_AGENT, userAgent)
                    .header(HttpHeaders.ACCEPT, "application/rss+xml,application/xml;q=0.9,*/*;q=0.5")
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();
            return RssParser.parse(bytes, "KAP", "tr");
        } catch (Exception e) {
            log.warn("KAP RSS fetch failed url={} err={}", rssUrl, e.getMessage());
            return List.of();
        }
    }
}
