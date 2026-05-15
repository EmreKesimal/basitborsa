package com.basitborsa.provider;

import com.basitborsa.dto.stock.StockPriceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(2)
public class FallbackMarketDataProvider implements MarketDataProvider {

    private static final Logger log = LoggerFactory.getLogger(FallbackMarketDataProvider.class);

    @Override
    public String getProviderName() {
        return "FALLBACK";
    }

    @Override
    public List<String> getSupportedSymbols() {
        return List.of("THYAO", "ASELS", "BIMAS", "SISE", "TUPRS");
    }

    @Override
    public StockPriceDto getLatestPrice(String symbol) {
        log.debug("FallbackProvider: returning null for {}, use seed data", symbol);
        return null;
    }

    @Override
    public List<StockPriceDto> getHistoricalPrices(String symbol, int days) {
        log.debug("FallbackProvider: returning empty list for {}, use seed data", symbol);
        return List.of();
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
