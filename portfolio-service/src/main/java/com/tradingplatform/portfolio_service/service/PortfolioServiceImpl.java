package com.tradingplatform.portfolio_service.service;

import com.tradingplatform.portfolio_service.client.MarketDataServiceClient;
import com.tradingplatform.portfolio_service.client.WalletServiceClient;
import com.tradingplatform.portfolio_service.dto.HoldingResponseDto;
import com.tradingplatform.portfolio_service.dto.PortfolioResponseDto;
import com.tradingplatform.portfolio_service.dto.PortfolioSummaryDto;
import com.tradingplatform.portfolio_service.exception.InsufficientSharesException;
import com.tradingplatform.portfolio_service.exception.PortfolioNotFoundException;
import com.tradingplatform.portfolio_service.model.Holding;
import com.tradingplatform.portfolio_service.model.Portfolio;
import com.tradingplatform.portfolio_service.repository.HoldingRepository;
import com.tradingplatform.portfolio_service.repository.PortfolioRepository;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private MarketDataServiceClient marketDataServiceClient;

    @Autowired
    private WalletServiceClient walletServiceClient;

    @Transactional
    @Override
    public PortfolioResponseDto createPortfolio(Long userId) {
        log.info("Creating portfolio for user {}", userId);

        if (portfolioRepository.existsByUserId(userId)) {
            log.warn("Portfolio already exists for user {}", userId);
            return getPortfolio(userId);
        }

        Portfolio portfolio = new Portfolio();
        portfolio.setUserId(userId);
        portfolio.setTotalValue(BigDecimal.ZERO);
        portfolio.setCashBalance(BigDecimal.ZERO);
        portfolio.setInvestedAmount(BigDecimal.ZERO);
        portfolio.setTotalProfitLoss(BigDecimal.ZERO);

        Portfolio saved = portfolioRepository.save(portfolio);
        log.info("Portfolio created for user {}", userId);

        return mapToPortfolioResponse(saved);
    }

    @Override
    public PortfolioResponseDto getPortfolio(Long userId) {
        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new PortfolioNotFoundException(userId));

        return mapToPortfolioResponse(portfolio);
    }
    @Override
    public PortfolioSummaryDto getPortfolioSummary(Long userId) {
        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new PortfolioNotFoundException(userId));

        // Get current wallet balance
        BigDecimal cashBalance = BigDecimal.ZERO;
        try {
            WalletServiceClient.BalanceResponse walletBalance = walletServiceClient.getBalance(userId);
            cashBalance = walletBalance.balance;
        } catch (Exception e) {
            log.error("Failed to get wallet balance for user {}: {}", userId, e.getMessage());
        }

        // Calculate current holdings value
        BigDecimal holdingsValue = calculateTotalHoldingsValue(portfolio.getId());
        BigDecimal totalValue = cashBalance.add(holdingsValue);

        // Calculate total profit/loss
        BigDecimal totalProfitLoss = holdingsValue.subtract(portfolio.getInvestedAmount());
        BigDecimal totalProfitLossPercent = BigDecimal.ZERO;
        if (portfolio.getInvestedAmount().compareTo(BigDecimal.ZERO) > 0) {
            totalProfitLossPercent = totalProfitLoss
                    .divide(portfolio.getInvestedAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new PortfolioSummaryDto(
                userId,
                totalValue,
                cashBalance,
                portfolio.getInvestedAmount(),
                totalProfitLoss,
                totalProfitLossPercent,
                portfolio.getHoldings().size()
        );
    }

    @Override
    public List<HoldingResponseDto> getUserHoldings(Long userId) {
        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new PortfolioNotFoundException(userId));

        List<Holding> holdings = holdingRepository.findByPortfolioId(portfolio.getId());

        return holdings.stream()
                .map(this::mapToHoldingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public HoldingResponseDto getHoldingBySymbol(Long userId, String symbol) {
        Holding holding = holdingRepository.findByUserIdAndSymbol(userId, symbol.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Holding not found: " + symbol));

        return mapToHoldingResponse(holding);
    }

    @Transactional
    @Override
    public void processBuyOrder(Long userId, String symbol, Integer quantity, BigDecimal pricePerShare) {
        log.info("Processing BUY order for user {}: {} shares of {} at ${}",
                userId, quantity, symbol, pricePerShare);

        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Portfolio newPortfolio = new Portfolio();
                    newPortfolio.setUserId(userId);
                    return portfolioRepository.save(newPortfolio);
                });

        Holding holding = holdingRepository.findByPortfolioIdAndSymbol(portfolio.getId(), symbol)
                .orElseGet(() -> {
                    Holding newHolding = new Holding();
                    newHolding.setPortfolio(portfolio);
                    newHolding.setSymbol(symbol.toUpperCase());
                    newHolding.setQuantity(0);
                    newHolding.setAveragePrice(BigDecimal.ZERO);
                    newHolding.setTotalInvested(BigDecimal.ZERO);
                    return newHolding;
                });

        BigDecimal currentTotalInvested = holding.getAveragePrice()
                .multiply(new BigDecimal(holding.getQuantity()));
        BigDecimal newInvestment = pricePerShare.multiply(new BigDecimal(quantity));
        BigDecimal totalInvested = currentTotalInvested.add(newInvestment);

        int newQuantity = holding.getQuantity() + quantity;
        BigDecimal newAveragePrice = totalInvested.divide(new BigDecimal(newQuantity), 4, RoundingMode.HALF_UP);

        holding.setQuantity(newQuantity);
        holding.setAveragePrice(newAveragePrice);
        holding.setTotalInvested(totalInvested);

        holdingRepository.save(holding);

        portfolio.setInvestedAmount(portfolio.getInvestedAmount().add(newInvestment));
        portfolioRepository.save(portfolio);

        log.info("BUY order processed: {} now has {} shares of {} at avg price ${}",
                userId, newQuantity, symbol, newAveragePrice);
    }

    @Transactional
    @Override
    public void processSellOrder(Long userId, String symbol, Integer quantity, BigDecimal pricePerShare) {
        log.info("Processing SELL order for user {}: {} shares of {} at ${}",
                userId, quantity, symbol, pricePerShare);

        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new PortfolioNotFoundException(userId));

        Holding holding = holdingRepository.findByPortfolioIdAndSymbol(portfolio.getId(), symbol)
                .orElseThrow(() -> new InsufficientSharesException(symbol, 0, quantity));

        if (holding.getQuantity() < quantity) {
            throw new InsufficientSharesException(symbol, holding.getQuantity(), quantity);
        }

        BigDecimal costBasis = holding.getAveragePrice().multiply(new BigDecimal(quantity));
        BigDecimal saleProceeds = pricePerShare.multiply(new BigDecimal(quantity));
        BigDecimal profitLoss = saleProceeds.subtract(costBasis);

        log.info("SELL realizes P&L of ${} on {} shares of {}", profitLoss, quantity, symbol);

        int newQuantity = holding.getQuantity() - quantity;

        if (newQuantity == 0) {
            holdingRepository.delete(holding);
            log.info("All shares of {} sold, removing holding", symbol);
        } else {
            holding.setQuantity(newQuantity);
            BigDecimal newTotalInvested = holding.getAveragePrice()
                    .multiply(new BigDecimal(newQuantity));
            holding.setTotalInvested(newTotalInvested);
            holdingRepository.save(holding);
        }

        portfolio.setInvestedAmount(portfolio.getInvestedAmount().subtract(costBasis));
        portfolioRepository.save(portfolio);

        log.info("SELL order processed: {} now has {} shares of {}",
                userId, newQuantity, symbol);
    }

    // Helper methods

    private BigDecimal calculateTotalHoldingsValue(Long portfolioId) {
        List<Holding> holdings = holdingRepository.findByPortfolioId(portfolioId);

        BigDecimal totalValue = BigDecimal.ZERO;

        for (Holding holding : holdings) {
            try {
                BigDecimal currentPrice = marketDataServiceClient.getCurrentPrice(holding.getSymbol());
                BigDecimal value = currentPrice.multiply(new BigDecimal(holding.getQuantity()));
                totalValue = totalValue.add(value);
            } catch (FeignException e) {
                log.warn("Failed to get price for {}, using average price", holding.getSymbol());
                BigDecimal value = holding.getAveragePrice().multiply(new BigDecimal(holding.getQuantity()));
                totalValue = totalValue.add(value);
            }
        }

        return totalValue;
    }

    private PortfolioResponseDto mapToPortfolioResponse(Portfolio portfolio) {
        BigDecimal cashBalance = BigDecimal.ZERO;
        try {
            WalletServiceClient.BalanceResponse walletBalance = walletServiceClient.getBalance(portfolio.getUserId());
            cashBalance = walletBalance.balance;
        } catch (Exception e) {
            log.error("Failed to get wallet balance: {}", e.getMessage());
        }

        List<HoldingResponseDto> holdingResponses = holdingRepository.findByPortfolioId(portfolio.getId())
                .stream()
                .map(this::mapToHoldingResponse)
                .collect(Collectors.toList());

        // Calculate total holdings value
        BigDecimal holdingsValue = holdingResponses.stream()
                .map(HoldingResponseDto::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total profit/loss
        BigDecimal totalProfitLoss = holdingResponses.stream()
                .map(HoldingResponseDto::getProfitLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalValue = cashBalance.add(holdingsValue);

        // Calculate total profit/loss percentage
        BigDecimal totalProfitLossPercent = BigDecimal.ZERO;
        if (portfolio.getInvestedAmount().compareTo(BigDecimal.ZERO) > 0) {
            totalProfitLossPercent = totalProfitLoss
                    .divide(portfolio.getInvestedAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        PortfolioResponseDto response = new PortfolioResponseDto();
        response.setId(portfolio.getId());
        response.setUserId(portfolio.getUserId());
        response.setTotalValue(totalValue);
        response.setCashBalance(cashBalance);
        response.setInvestedAmount(portfolio.getInvestedAmount());
        response.setTotalProfitLoss(totalProfitLoss);
        response.setTotalProfitLossPercent(totalProfitLossPercent);
        response.setHoldings(holdingResponses);

        return response;
    }

    private HoldingResponseDto mapToHoldingResponse(Holding holding) {
        HoldingResponseDto response = new HoldingResponseDto();
        response.setId(holding.getId());
        response.setSymbol(holding.getSymbol());
        response.setQuantity(holding.getQuantity());
        response.setAveragePrice(holding.getAveragePrice());
        response.setTotalInvested(holding.getTotalInvested());

        // Get current price and company name
        try {
            MarketDataServiceClient.StockQuoteResponse quote =
                    marketDataServiceClient.getQuote(holding.getSymbol());

            response.setCompanyName(quote.companyName);
            response.setCurrentPrice(quote.price);

            BigDecimal currentValue = quote.price.multiply(new BigDecimal(holding.getQuantity()));
            response.setTotalValue(currentValue);

            // Calculate profit/loss
            BigDecimal profitLoss = currentValue.subtract(holding.getTotalInvested());
            response.setProfitLoss(profitLoss);

            // Calculate profit/loss percentage
            if (holding.getTotalInvested().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal profitLossPercent = profitLoss
                        .divide(holding.getTotalInvested(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP);
                response.setProfitLossPercent(profitLossPercent);
            } else {
                response.setProfitLossPercent(BigDecimal.ZERO);
            }

        } catch (FeignException e) {
            log.warn("Failed to get current price for {}, using average price", holding.getSymbol());
            response.setCompanyName(holding.getSymbol());
            response.setCurrentPrice(holding.getAveragePrice());
            response.setTotalValue(holding.getTotalInvested());
            response.setProfitLoss(BigDecimal.ZERO);
            response.setProfitLossPercent(BigDecimal.ZERO);
        }

        return response;
    }
}
