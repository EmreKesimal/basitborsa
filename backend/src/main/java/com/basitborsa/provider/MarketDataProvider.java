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
}
