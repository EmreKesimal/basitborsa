package com.basitborsa.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Hackathon MVP scope gate. Only listed symbols receive real provider sync,
 * real price serving, news matching, and AI chart-story generation. Everything
 * else is served as DEMO_LIMITED with educational copy.
 */
@Component
public class ActiveSymbolsConfig {

    private static final Logger log = LoggerFactory.getLogger(ActiveSymbolsConfig.class);
    public static final String DEMO_LIMITED_DISCLAIMER =
            "Bu demo sürümünde gerçek gecikmeli/gün sonu veri yalnızca THYAO için aktiftir.";
    public static final String DEMO_LIMITED_CHART_STORY =
            "Bu demo sürümünde grafik hikâyesi gerçek veriyle yalnızca THYAO için aktiftir.";

    @Value("${market.data.active-symbols:THYAO}")
    private List<String> marketActiveSymbols;

    @Value("${news.active-symbols:THYAO}")
    private List<String> newsActiveSymbols;

    private Set<String> marketSet;
    private Set<String> newsSet;

    @PostConstruct
    void init() {
        marketSet = normalize(marketActiveSymbols);
        newsSet = normalize(newsActiveSymbols);
        log.info("Active symbols — market={} news={}", marketSet, newsSet);
    }

    private static Set<String> normalize(List<String> raw) {
        if (raw == null || raw.isEmpty()) return Set.of();
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String s : raw) {
            if (s == null) continue;
            String t = s.trim().toUpperCase();
            if (!t.isEmpty()) out.add(t);
        }
        return Collections.unmodifiableSet(out);
    }

    public Set<String> marketSymbols() { return marketSet; }

    public Set<String> newsSymbols() { return newsSet; }

    public boolean isMarketActive(String symbol) {
        return symbol != null && marketSet.contains(symbol.toUpperCase());
    }

    public boolean isNewsActive(String symbol) {
        return symbol != null && newsSet.contains(symbol.toUpperCase());
    }
}
