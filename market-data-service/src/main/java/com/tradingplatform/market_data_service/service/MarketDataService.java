package com.tradingplatform.market_data_service.service;

import com.tradingplatform.market_data_service.dto.*;
import com.tradingplatform.market_data_service.model.Stock;

import java.math.BigDecimal;
import java.util.List;

public interface MarketDataService {

    StockQuoteResponseDto getQuote(String symbol);

    StockDetailsResponseDto getStockDetails(String symbol);

    StockSearchResponseDto searchStocks(String keywords);

    void refreshQuote(String symbol);

    WatchlistResponseDto addToWatchlist(WatchlistRequestDto request);

    void removeFromWatchlist(Long userId, String symbol);

    List<WatchlistResponseDto> getWatchlist(Long userId);

    Stock getStockFromDb(String symbol);

    BigDecimal getCurrentPrice(String symbol);
}
