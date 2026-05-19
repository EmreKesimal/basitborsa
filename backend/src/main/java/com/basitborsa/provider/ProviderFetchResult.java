package com.basitborsa.provider;

import com.basitborsa.dto.stock.StockPriceDto;

import java.util.List;

public record ProviderFetchResult(
    Status status,
    String providerSymbol,
    int httpStatus,
    String message,
    List<StockPriceDto> prices
) {
    public enum Status {
        OK,
        EMPTY,
        RATE_LIMIT,
        ERROR,
        UNSUPPORTED
    }

    public static ProviderFetchResult ok(String providerSymbol, List<StockPriceDto> prices) {
        return new ProviderFetchResult(Status.OK, providerSymbol, 200, null, prices);
    }

    public static ProviderFetchResult empty(String providerSymbol, String message) {
        return new ProviderFetchResult(Status.EMPTY, providerSymbol, 200, message, List.of());
    }

    public static ProviderFetchResult rateLimit(String providerSymbol, int httpStatus, String message) {
        return new ProviderFetchResult(Status.RATE_LIMIT, providerSymbol, httpStatus, message, List.of());
    }

    public static ProviderFetchResult error(String providerSymbol, int httpStatus, String message) {
        return new ProviderFetchResult(Status.ERROR, providerSymbol, httpStatus, message, List.of());
    }

    public static ProviderFetchResult unsupported(String providerSymbol, String message) {
        return new ProviderFetchResult(Status.UNSUPPORTED, providerSymbol, 0, message, List.of());
    }
}
