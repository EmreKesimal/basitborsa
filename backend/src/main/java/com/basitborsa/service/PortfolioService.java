package com.basitborsa.service;

import com.basitborsa.dto.portfolio.AddPortfolioItemRequest;
import com.basitborsa.dto.portfolio.PortfolioDto;
import com.basitborsa.entity.*;
import com.basitborsa.exception.ResourceNotFoundException;
import com.basitborsa.mapper.PortfolioMapper;
import com.basitborsa.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.basitborsa.util.AppConstants.DEMO_USERNAME;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final PortfolioMapper portfolioMapper;

    public PortfolioService(PortfolioRepository portfolioRepository,
                            PortfolioItemRepository portfolioItemRepository,
                            StockRepository stockRepository,
                            UserRepository userRepository,
                            PortfolioMapper portfolioMapper) {
        this.portfolioRepository = portfolioRepository;
        this.portfolioItemRepository = portfolioItemRepository;
        this.stockRepository = stockRepository;
        this.userRepository = userRepository;
        this.portfolioMapper = portfolioMapper;
    }

    @Transactional(readOnly = true)
    public PortfolioDto getDemoPortfolio() {
        Portfolio portfolio = getOrCreateDemoPortfolio();
        return portfolioMapper.toDto(portfolio);
    }

    @Transactional
    public PortfolioDto addItem(AddPortfolioItemRequest request) {
        Portfolio portfolio = getOrCreateDemoPortfolio();
        Stock stock = stockRepository.findBySymbol(request.symbol().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Hisse bulunamadı: " + request.symbol()));

        BigDecimal purchasePrice = stock.getCurrentPrice();
        if (purchasePrice == null) {
            throw new IllegalArgumentException("Bu hisse için güncel fiyat bilgisi bulunamadı.");
        }

        BigDecimal totalCost = purchasePrice.multiply(BigDecimal.valueOf(request.quantity()));
        if (portfolio.getVirtualBalance().compareTo(totalCost) < 0) {
            throw new IllegalArgumentException("Yetersiz sanal bakiye.");
        }

        var existingItem = portfolioItemRepository.findByPortfolioAndStock(portfolio, stock);
        if (existingItem.isPresent()) {
            PortfolioItem item = existingItem.get();
            BigDecimal existingCost = item.getAveragePrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            int newQuantity = item.getQuantity() + request.quantity();
            BigDecimal newAvgPrice = existingCost.add(totalCost)
                    .divide(BigDecimal.valueOf(newQuantity), 4, RoundingMode.HALF_UP);
            item.setQuantity(newQuantity);
            item.setAveragePrice(newAvgPrice);
        } else {
            PortfolioItem newItem = new PortfolioItem();
            newItem.setPortfolio(portfolio);
            newItem.setStock(stock);
            newItem.setQuantity(request.quantity());
            newItem.setAveragePrice(purchasePrice);
            portfolio.getItems().add(newItem);
        }

        portfolio.setVirtualBalance(portfolio.getVirtualBalance().subtract(totalCost));
        portfolioRepository.save(portfolio);
        return portfolioMapper.toDto(portfolio);
    }

    @Transactional
    public PortfolioDto removeItem(Long itemId) {
        PortfolioItem item = portfolioItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Portföy kalemi bulunamadı: " + itemId));

        Portfolio portfolio = item.getPortfolio();
        BigDecimal refund = item.getStock().getCurrentPrice() != null
                ? item.getStock().getCurrentPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                : item.getAveragePrice().multiply(BigDecimal.valueOf(item.getQuantity()));

        portfolio.setVirtualBalance(portfolio.getVirtualBalance().add(refund));
        portfolio.getItems().remove(item);
        portfolioItemRepository.delete(item);
        portfolioRepository.save(portfolio);
        return portfolioMapper.toDto(portfolio);
    }

    private Portfolio getOrCreateDemoPortfolio() {
        User demoUser = userRepository.findByUsername(DEMO_USERNAME)
                .orElseThrow(() -> new ResourceNotFoundException("Demo kullanıcı bulunamadı. Seed data yüklü mü?"));
        return portfolioRepository.findByUser(demoUser).orElseGet(() -> {
            Portfolio p = new Portfolio();
            p.setUser(demoUser);
            return portfolioRepository.save(p);
        });
    }
}
