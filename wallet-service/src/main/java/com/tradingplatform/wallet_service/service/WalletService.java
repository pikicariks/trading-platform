package com.tradingplatform.wallet_service.service;

import com.tradingplatform.wallet_service.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {

    WalletResponseDto createWallet(CreateWalletRequestDto request);

    WalletResponseDto getWalletByUserId(Long userId);

    public BalanceResponseDto getBalance(Long userId);

    boolean hasSufficientBalance(Long userId, BigDecimal amount);

    TransactionResponseDto deposit(Long userId, TransactionRequestDto request);

    TransactionResponseDto withdraw(Long userId, TransactionRequestDto request);

    TransactionResponseDto deductForPurchase(Long userId, TransactionRequestDto request);

    TransactionResponseDto creditFromSale(Long userId, TransactionRequestDto request);

    List<TransactionResponseDto> getTransactionHistory(Long userId);

    public Page<TransactionResponseDto> getTransactionHistory(Long userId, Pageable pageable);
}
