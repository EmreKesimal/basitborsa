package com.basitborsa.provider;

import com.basitborsa.config.ActiveSymbolsConfig;
import com.basitborsa.dto.stock.StockPriceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Component
@Order(1)
public class ExternalMarketDataProvider implements MarketDataProvider {

    private static final Logger log = LoggerFactory.getLogger(ExternalMarketDataProvider.class);

    private static final List<String> SUPPORTED_SYMBOLS =
        List.of("THYAO", "ASELS", "BIMAS", "SISE", "TUPRS", "KCHOL", "GARAN", "FROTO");

    // Twelve Data accepts plain BIST tickers with exchange=BIST. Keep mapping
    // explicit so we can override per-symbol if their listing diverges.
    private static final Map<String, String> PROVIDER_SYMBOL_MAP = Map.ofEntries(
        Map.entry("THYAO", "THYAO"),
        Map.entry("ASELS", "ASELS"),
        Map.entry("BIMAS", "BIMAS"),
        Map.entry("SISE",  "SISE"),
        Map.entry("TUPRS", "TUPRS"),
        Map.entry("KCHOL", "KCHOL"),
        Map.entry("GARAN", "GARAN"),
        Map.entry("FROTO", "FROTO")
    );

    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private final WebClient webClient;
    private final ActiveSymbolsConfig activeSymbols;

    @Value("${market.data.api.key:}")
    private String apiKey;

    @Value("${market.data.provider:fallback}")
    private String providerConfig;

    @Value("${market.data.exchange:BIST}")
    private String exchange;

    public ExternalMarketDataProvider(
            @Value("${market.data.base-url:https://api.twelvedata.com}") String baseUrl,
            WebClient.Builder webClientBuilder,
            ActiveSymbolsConfig activeSymbols) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.activeSymbols = activeSymbols;
    }

    @Override
    public String getProviderName() {
        return "EXTERNAL_PROVIDER";
    }

    @Override
    public List<String> getSupportedSymbols() {
        return SUPPORTED_SYMBOLS;
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank()
            && "twelve-data".equalsIgnoreCase(providerConfig);
    }

    @Override
    public String mapToProviderSymbol(String symbol) {
        return PROVIDER_SYMBOL_MAP.getOrDefault(symbol, symbol);
    }

    @Override
    public StockPriceDto getLatestPrice(String symbol) {
        ProviderFetchResult res = getHistoricalPricesDetailed(symbol, 1);
        List<StockPriceDto> prices = res.prices();
        return prices.isEmpty() ? null : prices.get(prices.size() - 1);
    }

    @Override
    public List<StockPriceDto> getHistoricalPrices(String symbol, int days) {
        return getHistoricalPricesDetailed(symbol, days).prices();
    }

    @Override
    public ProviderFetchResult getHistoricalPricesDetailed(String symbol, int outputSize) {
        String providerSymbol = mapToProviderSymbol(symbol);

        if (!activeSymbols.isMarketActive(symbol)) {
            log.debug("Symbol {} not in active MVP scope; skipping provider call.", symbol);
            return ProviderFetchResult.unsupported(providerSymbol,
                    "symbol disabled for MVP demo scope");
        }
        if (!SUPPORTED_SYMBOLS.contains(symbol)) {
            log.warn("Symbol {} not supported by ExternalMarketDataProvider", symbol);
            return ProviderFetchResult.unsupported(providerSymbol, "symbol not supported by provider");
        }

        String safeUrl = String.format(
            "/time_series?symbol=%s&exchange=%s&interval=1day&outputsize=%d&apikey=***",
            providerSymbol, exchange, outputSize);
        log.info("Twelve Data request: symbol={} providerSymbol={} exchange={} url={}",
                symbol, providerSymbol, exchange, safeUrl);

        try {
            final String exchangeParam = this.exchange;
            TwelveDataResponse response = webClient.get()
                .uri(u -> u.path("/time_series")
                    .queryParam("symbol", providerSymbol)
                    .queryParam("exchange", exchangeParam)
                    .queryParam("interval", "1day")
                    .queryParam("outputsize", outputSize)
                    .queryParam("apikey", apiKey)
                    .build())
                .retrieve()
                .bodyToMono(TwelveDataResponse.class)
                .timeout(TIMEOUT)
                .block();

            if (response == null) {
                log.warn("Twelve Data null response: symbol={} providerSymbol={}", symbol, providerSymbol);
                return ProviderFetchResult.error(providerSymbol, 200, "null provider response");
            }

            String status = response.status() == null ? "" : response.status().toLowerCase(Locale.ROOT);
            if (!"ok".equals(status)) {
                int code = response.code() != null ? response.code() : 0;
                String msg = response.message() != null ? response.message() : "provider error";
                log.warn("Twelve Data non-ok status: symbol={} providerSymbol={} code={} status={} message={}",
                        symbol, providerSymbol, code, response.status(), msg);
                if (isRateLimitMessage(code, msg)) {
                    return ProviderFetchResult.rateLimit(providerSymbol, code == 0 ? 429 : code, msg);
                }
                return ProviderFetchResult.error(providerSymbol, code == 0 ? 400 : code, msg);
            }

            if (response.values() == null || response.values().isEmpty()) {
                log.warn("Twelve Data empty values: symbol={} providerSymbol={} exchange={}",
                        symbol, providerSymbol, exchange);
                return ProviderFetchResult.empty(providerSymbol, "empty provider response");
            }

            List<StockPriceDto> result = response.values().stream()
                .map(v -> mapToDto(symbol, v))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(StockPriceDto::date))
                .toList();

            log.info("Twelve Data parsed: symbol={} providerSymbol={} parsedRecords={}",
                    symbol, providerSymbol, result.size());

            if (result.isEmpty()) {
                return ProviderFetchResult.empty(providerSymbol, "parsed 0 valid rows");
            }
            return ProviderFetchResult.ok(providerSymbol, result);

        } catch (WebClientResponseException wcre) {
            int code = wcre.getStatusCode().value();
            String body = wcre.getResponseBodyAsString();
            log.warn("Twelve Data HTTP error: symbol={} providerSymbol={} httpStatus={} body={}",
                    symbol, providerSymbol, code, body);
            if (code == 429 || isRateLimitMessage(code, body)) {
                return ProviderFetchResult.rateLimit(providerSymbol, code, "rate limit / quota exceeded");
            }
            return ProviderFetchResult.error(providerSymbol, code, body);
        } catch (Exception e) {
            log.error("Twelve Data fetch failed: symbol={} providerSymbol={} error={}",
                    symbol, providerSymbol, e.getMessage());
            return ProviderFetchResult.error(providerSymbol, 0, e.getMessage());
        }
    }

    private boolean isRateLimitMessage(int code, String msg) {
        if (code == 429) return true;
        if (msg == null) return false;
        String m = msg.toLowerCase(Locale.ROOT);
        return m.contains("limit") || m.contains("quota") || m.contains("credit")
            || m.contains("too many") || m.contains("upgrade your plan");
    }

    private StockPriceDto mapToDto(String symbol, TwelveDataValue v) {
        try {
            LocalDate date = LocalDate.parse(v.datetime());
            BigDecimal open  = parseBigDecimal(v.open());
            BigDecimal high  = parseBigDecimal(v.high());
            BigDecimal low   = parseBigDecimal(v.low());
            BigDecimal close = parseBigDecimal(v.close());
            Long volume = v.volume() != null && !v.volume().isBlank()
                ? Long.parseLong(v.volume()) : null;

            if (close == null) return null;
            return new StockPriceDto(date, open, high, low, close, volume);
        } catch (Exception e) {
            log.warn("Failed to map Twelve Data value for {}: {}", symbol, e.getMessage());
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    record TwelveDataResponse(String status, String message, Integer code, List<TwelveDataValue> values) {}

    record TwelveDataValue(
        String datetime,
        String open,
        String high,
        String low,
        String close,
        String volume
    ) {}
}
