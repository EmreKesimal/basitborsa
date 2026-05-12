package com.basitborsa.controller;

import com.basitborsa.dto.stock.StockDto;
import com.basitborsa.dto.stock.StockEventDto;
import com.basitborsa.dto.stock.StockPriceHistoryDto;
import com.basitborsa.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.basitborsa.util.AppConstants.DEFAULT_PRICE_HISTORY_DAYS;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public ResponseEntity<List<StockDto>> getAllStocks() {
        return ResponseEntity.ok(stockService.getAllStocks());
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<StockDto> getStock(@PathVariable String symbol) {
        return ResponseEntity.ok(stockService.getStock(symbol));
    }

    @GetMapping("/{symbol}/events")
    public ResponseEntity<List<StockEventDto>> getEvents(@PathVariable String symbol) {
        return ResponseEntity.ok(stockService.getEvents(symbol));
    }

    @GetMapping("/{symbol}/prices")
    public ResponseEntity<StockPriceHistoryDto> getPriceHistory(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "30d") String range) {
        int days = parseRange(range);
        return ResponseEntity.ok(stockService.getPriceHistory(symbol, days));
    }

    private int parseRange(String range) {
        return switch (range) {
            case "7d" -> 7;
            case "30d" -> 30;
            case "90d" -> 90;
            case "180d" -> 180;
            case "1y" -> 365;
            default -> DEFAULT_PRICE_HISTORY_DAYS;
        };
    }
}
