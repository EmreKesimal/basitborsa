package com.basitborsa.controller;

import com.basitborsa.dto.stock.StockDto;
import com.basitborsa.dto.stock.StockEventDto;
import com.basitborsa.dto.stock.StockNewsDto;
import com.basitborsa.dto.stock.StockPriceHistoryDto;
import com.basitborsa.entity.StockNews;
import com.basitborsa.service.NewsService;
import com.basitborsa.service.StockService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.basitborsa.util.AppConstants.DEFAULT_PRICE_HISTORY_DAYS;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;
    private final NewsService newsService;

    public StockController(StockService stockService, NewsService newsService) {
        this.stockService = stockService;
        this.newsService = newsService;
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

    @GetMapping("/{symbol}/news")
    public ResponseEntity<List<StockNewsDto>> getNews(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "10") int limit) {
        List<StockNewsDto> rows = newsService.getRecent(symbol, limit).stream()
                .map(StockController::toDto)
                .toList();
        return ResponseEntity.ok(rows);
    }

    @GetMapping("/{symbol}/news/near")
    public ResponseEntity<List<StockNewsDto>> getNewsNear(
            @PathVariable String symbol,
            @RequestParam(name = "date", required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "before") String direction,
            @RequestParam(defaultValue = "5") int limit) {
        List<StockNewsDto> rows = newsService.getNear(symbol, date, direction, limit).stream()
                .map(StockController::toDto)
                .toList();
        return ResponseEntity.ok(rows);
    }

    private static StockNewsDto toDto(StockNews n) {
        return new StockNewsDto(
                n.getId(), n.getSymbol(), n.getTitle(), n.getSummary(),
                n.getSourceName(), n.getSourceUrl(), n.getPublishedAt(),
                n.getCategory(), n.getSourceType());
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
