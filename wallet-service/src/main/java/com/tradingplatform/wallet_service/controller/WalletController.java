package com.tradingplatform.wallet_service.controller;

import com.tradingplatform.wallet_service.dto.*;
import com.tradingplatform.wallet_service.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Wallet Service is running!");
    }

    @PostMapping("/create")
    public ResponseEntity<WalletResponseDto> createWallet(@Valid @RequestBody CreateWalletRequestDto request) {
        WalletResponseDto response = walletService.createWallet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<WalletResponseDto> getWallet(@PathVariable("userId") Long userId) {
        WalletResponseDto response = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/balance")
    public ResponseEntity<BalanceResponseDto> getBalance(@PathVariable("userId") Long userId) {
        BalanceResponseDto response = walletService.getBalance(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/check-balance")
    public ResponseEntity<BalanceResponseDto> checkSufficientBalance(
            @PathVariable("userId") Long userId,
            @RequestParam BigDecimal amount
    ) {
        BalanceResponseDto balance = walletService.getBalance(userId);
        balance.setHasSufficientFunds(walletService.hasSufficientBalance(userId, amount));
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/user/{userId}/deposit")
    public ResponseEntity<TransactionResponseDto> deposit(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody TransactionRequestDto request
    ) {
        TransactionResponseDto response = walletService.deposit(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/{userId}/withdraw")
    public ResponseEntity<TransactionResponseDto> withdraw(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody TransactionRequestDto request
    ) {
        TransactionResponseDto response = walletService.withdraw(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/{userId}/deduct")
    public ResponseEntity<TransactionResponseDto> deductForPurchase(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody TransactionRequestDto request
    ) {
        TransactionResponseDto response = walletService.deductForPurchase(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/{userId}/credit")
    public ResponseEntity<TransactionResponseDto> creditFromSale(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody TransactionRequestDto request
    ) {
        TransactionResponseDto response = walletService.creditFromSale(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/transactions")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionHistory(@PathVariable("userId") Long userId) {
        List<TransactionResponseDto> transactions = walletService.getTransactionHistory(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}/transactions/paged")
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionHistoryPaged(
            @PathVariable("userId") Long userId,
            Pageable pageable
    ) {
        Page<TransactionResponseDto> transactions = walletService.getTransactionHistory(userId, pageable);
        return ResponseEntity.ok(transactions);
    }
}
