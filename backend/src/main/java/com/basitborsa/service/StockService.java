package com.basitborsa.service;

import com.basitborsa.dto.stock.StockDto;
import com.basitborsa.dto.stock.StockEventDto;
import com.basitborsa.dto.stock.StockPriceHistoryDto;
import com.basitborsa.entity.Stock;
import com.basitborsa.entity.StockPrice;
import com.basitborsa.exception.ResourceNotFoundException;
import com.basitborsa.mapper.StockMapper;
import com.basitborsa.repository.StockEventRepository;
import com.basitborsa.repository.StockPriceRepository;
import com.basitborsa.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.basitborsa.util.AppConstants.*;

@Service
@Transactional(readOnly = true)
public class StockService {

    private static final List<String> EXTERNAL_SOURCES = List.of("EXTERNAL_PROVIDER", "CACHED");

    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final StockEventRepository stockEventRepository;
    private final StockMapper stockMapper;

    public StockService(StockRepository stockRepository,
                        StockPriceRepository stockPriceRepository,
                        StockEventRepository stockEventRepository,
                        StockMapper stockMapper) {
        this.stockRepository = stockRepository;
        this.stockPriceRepository = stockPriceRepository;
        this.stockEventRepository = stockEventRepository;
        this.stockMapper = stockMapper;
    }

    public List<StockDto> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(stockMapper::toDto)
                .toList();
    }

    public StockDto getStock(String symbol) {
        Stock stock = findBySymbol(symbol);
        return stockMapper.toDto(stock);
    }

    public List<StockEventDto> getEvents(String symbol) {
        Stock stock = findBySymbol(symbol);
        return stockEventRepository.findByStockOrderByEventDateAsc(stock).stream()
                .map(stockMapper::toEventDto)
                .toList();
    }

    public StockPriceHistoryDto getPriceHistory(String symbol, int days) {
        Stock stock = findBySymbol(symbol);
        LocalDate cutoff = LocalDate.now().minusDays(days);
        LocalDateTime updatedAt = stock.getLastPriceUpdatedAt() != null
                ? stock.getLastPriceUpdatedAt() : LocalDateTime.now();

        // External/cached prices take precedence over seed rows
        var externalPrices = stockPriceRepository
                .findByStockAndPriceDateAfterAndDataSourceInOrderByPriceDateAsc(stock, cutoff, EXTERNAL_SOURCES)
                .stream()
                .filter(this::isValidOhlc)
                .map(stockMapper::toPriceDto)
                .toList();

        if (!externalPrices.isEmpty()) {
            return new StockPriceHistoryDto(symbol, "EXTERNAL_PROVIDER", false, updatedAt,
                    externalPrices, DATA_DISCLAIMER);
        }

        // Fallback: no external data — return seed prices
        var seedPrices = stockPriceRepository
                .findByStockAndPriceDateAfterOrderByPriceDateAsc(stock, cutoff)
                .stream()
                .map(stockMapper::toPriceDto)
                .toList();

        return new StockPriceHistoryDto(symbol, stock.getDataSource(), true, updatedAt,
                seedPrices, DATA_DISCLAIMER);
    }

    private boolean isValidOhlc(StockPrice p) {
        BigDecimal h = p.getHighPrice();
        BigDecimal l = p.getLowPrice();
        BigDecimal c = p.getClosePrice();
        BigDecimal o = p.getOpenPrice();
        if (c == null) return false;
        if (h == null || l == null) return true;
        boolean highGeqLow   = h.compareTo(l) >= 0;
        boolean highGeqClose = h.compareTo(c) >= 0;
        boolean lowLeqClose  = l.compareTo(c) <= 0;
        boolean highGeqOpen  = o == null || h.compareTo(o) >= 0;
        boolean lowLeqOpen   = o == null || l.compareTo(o) <= 0;
        return highGeqLow && highGeqClose && lowLeqClose && highGeqOpen && lowLeqOpen;
    }

    private Stock findBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Hisse bulunamadı: " + symbol));
    }
}
