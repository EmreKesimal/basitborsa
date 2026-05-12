package com.basitborsa.controller;

import com.basitborsa.dto.portfolio.AddPortfolioItemRequest;
import com.basitborsa.dto.portfolio.PortfolioDto;
import com.basitborsa.service.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping
    public ResponseEntity<PortfolioDto> getPortfolio() {
        return ResponseEntity.ok(portfolioService.getDemoPortfolio());
    }

    @PostMapping("/items")
    public ResponseEntity<PortfolioDto> addItem(@Valid @RequestBody AddPortfolioItemRequest request) {
        return ResponseEntity.ok(portfolioService.addItem(request));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<PortfolioDto> removeItem(@PathVariable Long id) {
        return ResponseEntity.ok(portfolioService.removeItem(id));
    }
}
