package com.basitborsa.mapper;

import com.basitborsa.dto.stock.*;
import com.basitborsa.entity.Stock;
import com.basitborsa.entity.StockEvent;
import com.basitborsa.entity.StockPrice;
import org.springframework.stereotype.Component;

import static com.basitborsa.util.AppConstants.DATA_DISCLAIMER;

@Component
public class StockMapper {

    public StockDto toDto(Stock stock) {
        return new StockDto(
            stock.getId(),
            stock.getSymbol(),
            stock.getCompanyName(),
            stock.getSector(),
            stock.getDescription(),
            stock.getCurrentPrice(),
            stock.getDailyChangePercent(),
            stock.getPeRatio(),
            stock.getPbRatio(),
            stock.getDividendYield(),
            stock.getMarketCapBillions(),
            stock.getDataSource(),
            stock.isFallback(),
            stock.getLastPriceUpdatedAt(),
            DATA_DISCLAIMER
        );
    }

    public StockPriceDto toPriceDto(StockPrice price) {
        return new StockPriceDto(
            price.getPriceDate(),
            price.getOpenPrice(),
            price.getHighPrice(),
            price.getLowPrice(),
            price.getClosePrice(),
            price.getVolume()
        );
    }

    public StockEventDto toEventDto(StockEvent event) {
        return new StockEventDto(
            event.getId(),
            event.getEventDate(),
            event.getTitle(),
            event.getEventType().name(),
            event.getPriceChangePercent(),
            event.getShortDescription(),
            event.getRelatedNews(),
            event.getLearningNote()
        );
    }
}
