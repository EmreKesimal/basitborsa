package com.basitborsa.provider.news;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Generic multi-feed RSS provider. Feeds configured via NEWS_RSS_FEEDS env as a
 * semicolon-separated list of entries: name|type|url
 *
 * type is one of COMPANY | SECTOR | COMMODITY | MACRO | GLOBAL. Unknown values fall back to COMPANY.
 * The feedType is carried on each NewsArticle so NewsSyncService can apply
 * the right matching rule (strict alias match for COMPANY, keyword-only for the rest).
 */
@Component
public class GenericRssNewsProvider implements NewsProvider {

    private static final Logger log = LoggerFactory.getLogger(GenericRssNewsProvider.class);

    private static final Set<String> ALLOWED_FEED_TYPES =
            Set.of("COMPANY", "SECTOR", "COMMODITY", "MACRO", "GLOBAL");

    private final WebClient webClient = WebClient.builder().build();

    @Value("${news.rss.feeds:}")
    private String feedsConfig;

    @Value("${news.fetch-timeout-seconds:10}")
    private long timeoutSeconds;

    @Value("${news.http.user-agent:Mozilla/5.0}")
    private String userAgent;

    @Override public String getProviderName() { return "GENERIC_RSS"; }

    @Override
    public boolean isAvailable() { return feedsConfig != null && !feedsConfig.isBlank(); }

    @Override
    public List<NewsArticle> fetchLatest() {
        if (!isAvailable()) return List.of();
        List<Feed> feeds = parseFeeds(feedsConfig);
        if (feeds.isEmpty()) return List.of();

        List<NewsArticle> all = new ArrayList<>();
        for (Feed feed : feeds) {
            try {
                byte[] bytes = webClient.get()
                        .uri(feed.url)
                        .header(HttpHeaders.USER_AGENT, userAgent)
                        .header(HttpHeaders.ACCEPT, "application/rss+xml,application/xml;q=0.9,*/*;q=0.5")
                        .retrieve()
                        .bodyToMono(byte[].class)
                        .timeout(Duration.ofSeconds(timeoutSeconds))
                        .block();
                List<NewsArticle> articles = RssParser.parse(bytes, feed.name, "tr");
                // Re-stamp feedType from the configured feed.
                List<NewsArticle> tagged = new ArrayList<>(articles.size());
                for (NewsArticle a : articles) {
                    tagged.add(new NewsArticle(
                            a.title(), a.summary(), a.url(), a.publishedAt(),
                            a.sourceName(), a.language(), a.rawCategory(), feed.type));
                }
                log.info("RSS fetched: feed={} type={} count={}", feed.name, feed.type, tagged.size());
                all.addAll(tagged);
            } catch (Exception e) {
                log.warn("RSS fetch failed feed={} url={} err={}", feed.name, feed.url, e.getMessage());
            }
        }
        return all;
    }

    static List<Feed> parseFeeds(String raw) {
        List<Feed> out = new ArrayList<>();
        if (raw == null || raw.isBlank()) return out;
        for (String token : raw.split(";")) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) continue;
            String[] parts = trimmed.split("\\|", 3);
            if (parts.length < 3) {
                continue;
            }
            String name = cleanPart(parts[0]);
            String typeRaw = cleanPart(parts[1]).toUpperCase();
            String url = cleanPart(parts[2]);
            if (name.isEmpty() || url.isEmpty()) continue;
            String type = ALLOWED_FEED_TYPES.contains(typeRaw) ? typeRaw : "COMPANY";
            out.add(new Feed(name, type, url));
        }
        return out;
    }

    /** Trim whitespace then strip a single pair of surrounding single/double quotes. */
    private static String cleanPart(String s) {
        if (s == null) return "";
        String t = s.trim();
        if (t.length() >= 2) {
            char first = t.charAt(0);
            char last = t.charAt(t.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                t = t.substring(1, t.length() - 1).trim();
            }
        }
        // Strip stray trailing quotes/whitespace (e.g. URLs accidentally ending with ")
        while (!t.isEmpty()) {
            char c = t.charAt(t.length() - 1);
            if (c == '"' || c == '\'' || Character.isWhitespace(c)) {
                t = t.substring(0, t.length() - 1);
            } else break;
        }
        while (!t.isEmpty()) {
            char c = t.charAt(0);
            if (c == '"' || c == '\'' || Character.isWhitespace(c)) {
                t = t.substring(1);
            } else break;
        }
        return t;
    }

    record Feed(String name, String type, String url) {}
}
