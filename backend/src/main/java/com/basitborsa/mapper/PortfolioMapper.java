package com.basitborsa.mapper;

import com.basitborsa.dto.portfolio.PortfolioDto;
import com.basitborsa.dto.portfolio.PortfolioItemDto;
import com.basitborsa.entity.Portfolio;
import com.basitborsa.entity.PortfolioItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.basitborsa.util.AppConstants.DATA_DISCLAIMER;

@Component
public class PortfolioMapper {

    public PortfolioItemDto toItemDto(PortfolioItem item) {
        BigDecimal currentPrice = item.getStock().getCurrentPrice() != null
                ? item.getStock().getCurrentPrice()
                : item.getAveragePrice();
        BigDecimal totalCost = item.getAveragePrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        BigDecimal currentValue = currentPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
        BigDecimal gainLoss = currentValue.subtract(totalCost);
        BigDecimal gainLossPercent = totalCost.compareTo(BigDecimal.ZERO) != 0
                ? gainLoss.divide(totalCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return new PortfolioItemDto(
            item.getId(),
            item.getStock().getSymbol(),
            item.getStock().getCompanyName(),
            item.getQuantity(),
            item.getAveragePrice(),
            currentPrice,
            totalCost,
            currentValue,
            gainLoss,
            gainLossPercent
        );
    }

    public PortfolioDto toDto(Portfolio portfolio) {
        List<PortfolioItemDto> itemDtos = portfolio.getItems().stream()
                .map(this::toItemDto)
                .toList();

        BigDecimal totalStockValue = itemDtos.stream()
                .map(PortfolioItemDto::currentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCostBasis = itemDtos.stream()
                .map(PortfolioItemDto::totalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalValue = portfolio.getVirtualBalance().add(totalStockValue);
        BigDecimal totalGainLoss = totalStockValue.subtract(totalCostBasis);
        BigDecimal totalGainLossPercent = totalCostBasis.compareTo(BigDecimal.ZERO) != 0
                ? totalGainLoss.divide(totalCostBasis, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return new PortfolioDto(
            portfolio.getId(),
            portfolio.getVirtualBalance(),
            totalStockValue,
            totalValue,
            totalGainLoss,
            totalGainLossPercent,
            itemDtos,
            DATA_DISCLAIMER
        );
    }
}
