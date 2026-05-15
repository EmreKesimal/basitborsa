package com.basitborsa.provider;

import com.basitborsa.dto.stock.StockPriceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
@Order(1)
public class ExternalMarketDataProvider implements MarketDataProvider {

    private static final Logger log = LoggerFactory.getLogger(ExternalMarketDataProvider.class);

    private static final List<String> SUPPORTED_SYMBOLS =
        List.of("THYAO", "ASELS", "BIMAS", "SISE", "TUPRS", "KCHOL", "GARAN", "FROTO");

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final WebClient webClient;

    @Value("${market.data.api.key:}")
    private String apiKey;

    @Value("${market.data.provider:fallback}")
    private String providerConfig;

    @Value("${market.data.exchange:BIST}")
    private String exchange;

    public ExternalMarketDataProvider(
            @Value("${market.data.base-url:https://api.twelvedata.com}") String baseUrl,
            WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
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
    public StockPriceDto getLatestPrice(String symbol) {
        List<StockPriceDto> prices = fetchTimeSeries(symbol, 1);
        return prices.isEmpty() ? null : prices.get(prices.size() - 1);
    }

    @Override
    public List<StockPriceDto> getHistoricalPrices(String symbol, int days) {
        return fetchTimeSeries(symbol, days);
    }

    private List<StockPriceDto> fetchTimeSeries(String symbol, int outputSize) {
        if (!SUPPORTED_SYMBOLS.contains(symbol)) {
            log.warn("Symbol {} not supported by ExternalMarketDataProvider", symbol);
            return List.of();
        }

        log.info("Fetching Twelve Data: provider=EXTERNAL_PROVIDER exchange={} symbol={} outputSize={}",
                exchange, symbol, outputSize);

        try {
            final String exchangeParam = this.exchange;
            TwelveDataResponse response = webClient.get()
                .uri(u -> u.path("/time_series")
                    .queryParam("symbol", symbol)
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
                log.warn("Twelve Data null response: symbol={} exchange={}", symbol, exchange);
                return List.of();
            }
            if (!"ok".equalsIgnoreCase(response.status())) {
                log.warn("Twelve Data error: symbol={} exchange={} status={} message={}",
                        symbol, exchange, response.status(), response.message());
                return List.of();
            }
            if (response.values() == null || response.values().isEmpty()) {
                log.warn("Twelve Data empty values: symbol={} exchange={}", symbol, exchange);
                return List.of();
            }

            List<StockPriceDto> result = response.values().stream()
                .map(v -> mapToDto(symbol, v))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(StockPriceDto::date))
                .toList();

            log.info("Twelve Data fetched: symbol={} exchange={} records={}", symbol, exchange, result.size());
            return result;

        } catch (Exception e) {
            log.error("Twelve Data fetch failed: symbol={} exchange={} error={}", symbol, exchange, e.getMessage());
            return List.of();
        }
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

    // Twelve Data /time_series response shape
    record TwelveDataResponse(String status, String message, List<TwelveDataValue> values) {}

    record TwelveDataValue(
        String datetime,
        String open,
        String high,
        String low,
        String close,
        String volume
    ) {}
}
