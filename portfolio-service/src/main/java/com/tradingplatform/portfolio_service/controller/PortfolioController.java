package com.tradingplatform.portfolio_service.controller;

import com.tradingplatform.portfolio_service.dto.HoldingResponseDto;
import com.tradingplatform.portfolio_service.dto.PortfolioResponseDto;
import com.tradingplatform.portfolio_service.dto.PortfolioSummaryDto;
import com.tradingplatform.portfolio_service.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Portfolio Service is running!");
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<PortfolioResponseDto> createPortfolio(@PathVariable("userId") Long userId) {
        PortfolioResponseDto response = portfolioService.createPortfolio(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PortfolioResponseDto> getPortfolio(@PathVariable("userId") Long userId) {
        PortfolioResponseDto response = portfolioService.getPortfolio(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<PortfolioSummaryDto> getPortfolioSummary(@PathVariable("userId") Long userId) {
        PortfolioSummaryDto summary = portfolioService.getPortfolioSummary(userId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/user/{userId}/holdings")
    public ResponseEntity<List<HoldingResponseDto>> getUserHoldings(@PathVariable("userId") Long userId) {
        List<HoldingResponseDto> holdings = portfolioService.getUserHoldings(userId);
        return ResponseEntity.ok(holdings);
    }

    @GetMapping("/user/{userId}/holdings/{symbol}")
    public ResponseEntity<HoldingResponseDto> getHoldingBySymbol(
            @PathVariable("userId") Long userId,
            @PathVariable("symbol") String symbol
    ) {
        HoldingResponseDto holding = portfolioService.getHoldingBySymbol(userId, symbol);
        return ResponseEntity.ok(holding);
    }
}
