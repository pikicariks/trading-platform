package com.tradingplatform.market_data_service.service;

import com.tradingplatform.market_data_service.client.AlphaVantageClient;
import com.tradingplatform.market_data_service.dto.*;
import com.tradingplatform.market_data_service.exception.StockNotFoundException;
import com.tradingplatform.market_data_service.model.Stock;
import com.tradingplatform.market_data_service.model.Watchlist;
import com.tradingplatform.market_data_service.repository.StockRepository;
import com.tradingplatform.market_data_service.repository.WatchlistRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataServiceImpl implements MarketDataService {

    @Autowired
    private final AlphaVantageClient alphaVantageClient;

    @Autowired
    private final StockRepository stockRepository;

    @Autowired
    private final WatchlistRepository watchlistRepository;

    @Override
    @Cacheable(value = "stockQuotes", key = "#symbol != null ? #symbol.toUpperCase() : 'NULL'")
    public StockQuoteResponseDto getQuote(String symbol) {
        validateSymbol(symbol);
        log.info("Getting quote for symbol: {}", symbol);

        StockQuoteResponseDto quote = alphaVantageClient.getQuote(symbol.toUpperCase());

        updateStockFromQuote(quote);

        return quote;
    }

    @Override
    @Cacheable(value = "stockDetails", key = "#symbol != null ? #symbol.toUpperCase() : 'NULL'")
    public StockDetailsResponseDto getStockDetails(String symbol) {
        validateSymbol(symbol);
        log.info("Getting details for symbol: {}", symbol);

        StockDetailsResponseDto details = alphaVantageClient.getCompanyOverview(symbol.toUpperCase());
        
        updateStockFromDetails(details);

        return details;
    }

    @Override
    @Cacheable(value = "stockSearch", key = "#keywords != null ? #keywords.toLowerCase() : 'NULL'")
    public StockSearchResponseDto searchStocks(String keywords) {
        validateKeywords(keywords);
        log.info("Searching stocks with keywords: {}", keywords);
        return alphaVantageClient.searchSymbol(keywords);
    }

    @Override
    @CacheEvict(value = "stockQuotes", key = "#symbol != null ? #symbol.toUpperCase() : 'NULL'")
    public void refreshQuote(String symbol) {
        validateSymbol(symbol);
        log.info("Refreshing quote cache for symbol: {}", symbol);
    }

    // Watchlist methods
    @Override
    @Transactional
    public WatchlistResponseDto addToWatchlist(WatchlistRequestDto request) {
        String symbol = request.getSymbol().toUpperCase();

        if (watchlistRepository.existsByUserIdAndSymbol(request.getUserId(), symbol)) {
            throw new RuntimeException("Stock already in watchlist");
        }

        StockQuoteResponseDto quote = getQuote(symbol);

        Watchlist watchlist = new Watchlist();
        watchlist.setUserId(request.getUserId());
        watchlist.setSymbol(symbol);
        watchlist.setNotes(request.getNotes());

        Watchlist saved = watchlistRepository.save(watchlist);
        log.info("Added {} to watchlist for user {}", symbol, request.getUserId());

        return mapToWatchlistResponseDto(saved, quote);
    }

    @Override
    @Transactional
    public void removeFromWatchlist(Long userId, String symbol) {
        symbol = symbol.toUpperCase();

        if (!watchlistRepository.existsByUserIdAndSymbol(userId, symbol)) {
            throw new StockNotFoundException("Stock not in watchlist: " + symbol);
        }

        watchlistRepository.deleteByUserIdAndSymbol(userId, symbol);
        log.info("Removed {} from watchlist for user {}", symbol, userId);
    }

    @Override
    public List<WatchlistResponseDto> getWatchlist(Long userId) {
        List<Watchlist> watchlistItems = watchlistRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<WatchlistResponseDto> responses = new ArrayList<>();

        for (Watchlist item : watchlistItems) {
            try {
                StockQuoteResponseDto quote = getQuote(item.getSymbol());
                responses.add(mapToWatchlistResponseDto(item, quote));
            } catch (Exception e) {

                WatchlistResponseDto response = new WatchlistResponseDto();
                response.setId(item.getId());
                response.setUserId(item.getUserId());
                response.setSymbol(item.getSymbol());
                response.setNotes(item.getNotes());
                response.setAddedAt(item.getCreatedAt());
                responses.add(response);
            }
        }

        return responses;
    }

    @Override
    public Stock getStockFromDb(String symbol) {
        return stockRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new StockNotFoundException("Stock not found in database: " + symbol));
    }

    @Override
    public BigDecimal getCurrentPrice(String symbol) {
        StockQuoteResponseDto quote = getQuote(symbol);
        return quote.getPrice();
    }

    // Helper methods
    private void updateStockFromQuote(StockQuoteResponseDto quote) {
        Stock stock = stockRepository.findBySymbol(quote.getSymbol())
                .orElse(new Stock());

        stock.setSymbol(quote.getSymbol());
        stock.setCurrentPrice(quote.getPrice());
        stock.setPreviousClose(quote.getPreviousClose());
        stock.setOpenPrice(quote.getOpen());
        stock.setDayHigh(quote.getDayHigh());
        stock.setDayLow(quote.getDayLow());
        stock.setVolume(quote.getVolume());
        stock.setLastUpdated(LocalDateTime.now());

        if (quote.getCompanyName() != null) {
            stock.setCompanyName(quote.getCompanyName());
        }

        stockRepository.save(stock);
    }

    private void updateStockFromDetails(StockDetailsResponseDto details) {
        Stock stock = stockRepository.findBySymbol(details.getSymbol())
                .orElse(new Stock());

        stock.setSymbol(details.getSymbol());
        stock.setCompanyName(details.getCompanyName());
        stock.setExchange(details.getExchange());
        stock.setSector(details.getSector());
        stock.setIndustry(details.getIndustry());
        stock.setMarketCap(details.getMarketCap());
        stock.setPeRatio(details.getPeRatio());
        stock.setDividendYield(details.getDividendYield());
        stock.setWeek52High(details.getWeek52High());
        stock.setWeek52Low(details.getWeek52Low());
        stock.setLastUpdated(LocalDateTime.now());

        stockRepository.save(stock);
    }

    private WatchlistResponseDto mapToWatchlistResponseDto(Watchlist watchlist, StockQuoteResponseDto quote) {
        WatchlistResponseDto response = new WatchlistResponseDto();
        response.setId(watchlist.getId());
        response.setUserId(watchlist.getUserId());
        response.setSymbol(watchlist.getSymbol());
        response.setCompanyName(quote.getCompanyName());
        response.setCurrentPrice(quote.getPrice());
        response.setChangePercent(quote.getChangePercent());
        response.setNotes(watchlist.getNotes());
        response.setAddedAt(watchlist.getCreatedAt());
        return response;
    }

    private void validateSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
    }

    private void validateKeywords(String keywords) {
        if (keywords == null || keywords.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keywords cannot be null or empty");
        }
    }
}
