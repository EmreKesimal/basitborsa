package com.basitborsa.service;

import com.basitborsa.dto.stock.StockDto;
import com.basitborsa.dto.stock.StockEventDto;
import com.basitborsa.dto.stock.StockPriceHistoryDto;
import com.basitborsa.entity.Stock;
import com.basitborsa.exception.ResourceNotFoundException;
import com.basitborsa.mapper.StockMapper;
import com.basitborsa.repository.StockEventRepository;
import com.basitborsa.repository.StockPriceRepository;
import com.basitborsa.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.basitborsa.util.AppConstants.*;

@Service
@Transactional(readOnly = true)
public class StockService {

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
        var prices = stockPriceRepository
                .findByStockAndPriceDateAfterOrderByPriceDateAsc(stock, cutoff)
                .stream()
                .map(stockMapper::toPriceDto)
                .toList();

        return new StockPriceHistoryDto(
            symbol,
            stock.getDataSource(),
            stock.isFallback(),
            stock.getLastPriceUpdatedAt() != null ? stock.getLastPriceUpdatedAt() : LocalDateTime.now(),
            prices,
            DATA_DISCLAIMER
        );
    }

    private Stock findBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Hisse bulunamadı: " + symbol));
    }
}
