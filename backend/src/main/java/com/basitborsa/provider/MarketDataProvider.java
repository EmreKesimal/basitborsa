package com.basitborsa.provider;

import com.basitborsa.dto.stock.StockDto;
import com.basitborsa.dto.stock.StockPriceDto;
import java.util.List;

public interface MarketDataProvider {
    String getProviderName();
    List<String> getSupportedSymbols();
    StockPriceDto getLatestPrice(String symbol);
    List<StockPriceDto> getHistoricalPrices(String symbol, int days);
    boolean isAvailable();

    /**
     * Detailed fetch result for sync flows. Default wraps getHistoricalPrices for
     * providers that don't implement detailed reporting (e.g. fallback).
     */
    default ProviderFetchResult getHistoricalPricesDetailed(String symbol, int days) {
        List<StockPriceDto> prices = getHistoricalPrices(symbol, days);
        if (prices == null || prices.isEmpty()) {
            return ProviderFetchResult.empty(symbol, "empty provider response");
        }
        return ProviderFetchResult.ok(symbol, prices);
    }

    /**
     * Per-provider symbol mapping. Default identity; providers may override
     * (e.g. add .IS suffix or exchange-specific ticker).
     */
    default String mapToProviderSymbol(String symbol) {
        return symbol;
    }
}
