package com.tradingplatform.market_data_service.service;

import com.tradingplatform.market_data_service.dto.*;
import com.tradingplatform.market_data_service.model.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class MockMarketDataService implements MarketDataService{
    @Override
    public StockQuoteResponseDto getQuote(String symbol) {
        return null;
    }

    @Override
    public StockDetailsResponseDto getStockDetails(String symbol) {
        return null;
    }

    @Override
    public StockSearchResponseDto searchStocks(String keywords) {
        return null;
    }

    @Override
    public void refreshQuote(String symbol) {

    }

    @Override
    public WatchlistResponseDto addToWatchlist(WatchlistRequestDto request) {
        return null;
    }

    @Override
    public void removeFromWatchlist(Long userId, String symbol) {

    }

    @Override
    public List<WatchlistResponseDto> getWatchlist(Long userId) {
        return List.of();
    }

    @Override
    public Stock getStockFromDb(String symbol) {
        return null;
    }

    @Override
    public BigDecimal getCurrentPrice(String symbol) {
        return null;
    }
}
