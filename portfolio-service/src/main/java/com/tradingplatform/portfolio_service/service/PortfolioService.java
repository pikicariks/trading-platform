package com.tradingplatform.portfolio_service.service;

import com.tradingplatform.portfolio_service.dto.HoldingResponseDto;
import com.tradingplatform.portfolio_service.dto.PortfolioResponseDto;
import com.tradingplatform.portfolio_service.dto.PortfolioSummaryDto;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface PortfolioService {

    @Transactional
    PortfolioResponseDto createPortfolio(Long userId);

    PortfolioResponseDto getPortfolio(Long userId);

    PortfolioSummaryDto getPortfolioSummary(Long userId);

    List<HoldingResponseDto> getUserHoldings(Long userId);

    HoldingResponseDto getHoldingBySymbol(Long userId, String symbol);

    @Transactional
    void processBuyOrder(Long userId, String symbol, Integer quantity, BigDecimal pricePerShare);

    @Transactional
    void processSellOrder(Long userId, String symbol, Integer quantity, BigDecimal pricePerShare);
}
