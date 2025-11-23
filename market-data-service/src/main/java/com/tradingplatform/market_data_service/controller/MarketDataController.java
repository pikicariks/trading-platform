package com.tradingplatform.market_data_service.controller;

import com.tradingplatform.market_data_service.dto.*;
import com.tradingplatform.market_data_service.service.MarketDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketDataController {

    @Autowired
    private MarketDataService marketDataService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Market Data Service is running!");
    }

    @GetMapping("/quote/{symbol}")
    public ResponseEntity<StockQuoteResponseDto> getQuote(@PathVariable("symbol") String symbol) {
        StockQuoteResponseDto quote = marketDataService.getQuote(symbol);
        return ResponseEntity.ok(quote);
    }

    @GetMapping("/details/{symbol}")
    public ResponseEntity<StockDetailsResponseDto> getStockDetails(@PathVariable("symbol") String symbol) {
        StockDetailsResponseDto details = marketDataService.getStockDetails(symbol);
        return ResponseEntity.ok(details);
    }

    @GetMapping("/search")
    public ResponseEntity<StockSearchResponseDto> searchStocks(@RequestParam("keywords") String keywords) {
        StockSearchResponseDto results = marketDataService.searchStocks(keywords);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/price/{symbol}")
    public ResponseEntity<BigDecimal> getCurrentPrice(@PathVariable("symbol") String symbol) {
        BigDecimal price = marketDataService.getCurrentPrice(symbol);
        return ResponseEntity.ok(price);
    }

    @PostMapping("/quote/{symbol}/refresh")
    public ResponseEntity<StockQuoteResponseDto> refreshQuote(@PathVariable("symbol") String symbol) {
        marketDataService.refreshQuote(symbol);
        StockQuoteResponseDto quote = marketDataService.getQuote(symbol);
        return ResponseEntity.ok(quote);
    }

    @PostMapping("/watchlist")
    public ResponseEntity<WatchlistResponseDto> addToWatchlist(@Valid @RequestBody WatchlistRequestDto request) {
        WatchlistResponseDto response = marketDataService.addToWatchlist(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/watchlist/user/{userId}")
    public ResponseEntity<List<WatchlistResponseDto>> getWatchlist(@PathVariable("userId") Long userId) {
        List<WatchlistResponseDto> watchlist = marketDataService.getWatchlist(userId);
        return ResponseEntity.ok(watchlist);
    }

    @DeleteMapping("/watchlist/user/{userId}/symbol/{symbol}")
    public ResponseEntity<Void> removeFromWatchlist(
            @PathVariable("userId") Long userId,
            @PathVariable("symbol") String symbol
    ) {
        marketDataService.removeFromWatchlist(userId, symbol);
        return ResponseEntity.noContent().build();
    }
}
