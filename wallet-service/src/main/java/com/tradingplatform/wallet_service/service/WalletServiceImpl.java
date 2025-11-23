package com.tradingplatform.wallet_service.service;

import com.tradingplatform.wallet_service.dto.*;
import com.tradingplatform.wallet_service.exception.InsufficientBalanceException;
import com.tradingplatform.wallet_service.exception.WalletException;
import com.tradingplatform.wallet_service.exception.WalletNotFoundException;
import com.tradingplatform.wallet_service.model.TransactionStatus;
import com.tradingplatform.wallet_service.model.TransactionType;
import com.tradingplatform.wallet_service.model.Wallet;
import com.tradingplatform.wallet_service.model.WalletTransaction;
import com.tradingplatform.wallet_service.repository.WalletRepository;
import com.tradingplatform.wallet_service.repository.WalletTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository transactionRepository;

    @Value("${wallet.starting-balance.basic}")
    private BigDecimal basicStartingBalance;

    @Value("${wallet.starting-balance.premium}")
    private BigDecimal premiumStartingBalance;

    @Value("${wallet.starting-balance.admin}")
    private BigDecimal adminStartingBalance;

    @Override
    @Transactional
    public WalletResponseDto createWallet(CreateWalletRequestDto request) {
        if (walletRepository.existsByUserId(request.getUserId())) {
            throw new WalletException("Wallet already exists for user: " + request.getUserId());
        }

        BigDecimal startingBalance = getStartingBalance(request.getRole());

        Wallet wallet = new Wallet();
        wallet.setUserId(request.getUserId());
        wallet.setBalance(startingBalance);
        wallet.setCurrency("USD");
        wallet.setIsActive(true);

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Created wallet for user {} with balance ${}", request.getUserId(), startingBalance);

        createTransaction(
                savedWallet.getId(),
                TransactionType.INITIAL_DEPOSIT,
                startingBalance,
                savedWallet.getBalance(),
                "Initial deposit based on " + request.getRole() + " account",
                null
        );

        return mapToWalletResponseDto(savedWallet);
    }

    @Override
    public WalletResponseDto getWalletByUserId(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));
        return mapToWalletResponseDto(wallet);
    }

    @Override
    public BalanceResponseDto getBalance(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));
        return new BalanceResponseDto(userId, wallet.getBalance(), wallet.getCurrency());
    }

    @Override
    public boolean hasSufficientBalance(Long userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));
        return wallet.getBalance().compareTo(amount) >= 0;
    }

    @Override
    @Transactional
    public TransactionResponseDto deposit(Long userId, TransactionRequestDto request) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));

        if (!wallet.getIsActive()) {
            throw new WalletException("Wallet is inactive");
        }

        BigDecimal newBalance = wallet.getBalance().add(request.getAmount());
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        log.info("Deposited ${} to user {} wallet. New balance: ${}",
                request.getAmount(), userId, newBalance);

        return createTransaction(
                wallet.getId(),
                TransactionType.DEPOSIT,
                request.getAmount(),
                newBalance,
                request.getDescription() != null ? request.getDescription() : "Deposit",
                request.getReferenceId()
        );
    }

    @Override
    @Transactional
    public TransactionResponseDto withdraw(Long userId, TransactionRequestDto request) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));

        if (!wallet.getIsActive()) {
            throw new WalletException("Wallet is inactive");
        }

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(request.getAmount(), wallet.getBalance());
        }

        BigDecimal newBalance = wallet.getBalance().subtract(request.getAmount());
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        log.info("Withdrew ${} from user {} wallet. New balance: ${}",
                request.getAmount(), userId, newBalance);

        return createTransaction(
                wallet.getId(),
                TransactionType.WITHDRAWAL,
                request.getAmount().negate(),
                newBalance,
                request.getDescription() != null ? request.getDescription() : "Withdrawal",
                request.getReferenceId()
        );
    }

    @Override
    @Transactional
    public TransactionResponseDto deductForPurchase(Long userId, TransactionRequestDto request) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));

        if (!wallet.getIsActive()) {
            throw new WalletException("Wallet is inactive");
        }

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(request.getAmount(), wallet.getBalance());
        }

        BigDecimal newBalance = wallet.getBalance().subtract(request.getAmount());
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        log.info("Deducted ${} from user {} for stock purchase. New balance: ${}",
                request.getAmount(), userId, newBalance);

        return createTransaction(
                wallet.getId(),
                TransactionType.BUY_STOCK,
                request.getAmount().negate(),
                newBalance,
                request.getDescription(),
                request.getReferenceId()
        );
    }

    @Override
    @Transactional
    public TransactionResponseDto creditFromSale(Long userId, TransactionRequestDto request) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));

        if (!wallet.getIsActive()) {
            throw new WalletException("Wallet is inactive");
        }

        BigDecimal newBalance = wallet.getBalance().add(request.getAmount());
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        log.info("Credited ${} to user {} from stock sale. New balance: ${}",
                request.getAmount(), userId, newBalance);

        return createTransaction(
                wallet.getId(),
                TransactionType.SELL_STOCK,
                request.getAmount(),
                newBalance,
                request.getDescription(),
                request.getReferenceId()
        );
    }

    @Override
    public List<TransactionResponseDto> getTransactionHistory(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));

        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId())
                .stream()
                .map(this::mapToTransactionResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<TransactionResponseDto> getTransactionHistory(Long userId, Pageable pageable) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));

        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable)
                .map(this::mapToTransactionResponseDto);
    }

    private BigDecimal getStartingBalance(String role) {
        return switch (role.toUpperCase()) {
            case "PREMIUM" -> premiumStartingBalance;
            case "ADMIN" -> adminStartingBalance;
            default -> basicStartingBalance;
        };
    }

    private TransactionResponseDto createTransaction(
            Long walletId,
            TransactionType type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String description,
            String referenceId
    ) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(walletId);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription(description);
        transaction.setReferenceId(referenceId);
        transaction.setStatus(TransactionStatus.COMPLETED);

        WalletTransaction saved = transactionRepository.save(transaction);
        return mapToTransactionResponseDto(saved);
    }

    private WalletResponseDto mapToWalletResponseDto(Wallet wallet) {
        return new WalletResponseDto(
                wallet.getId(),
                wallet.getUserId(),
                wallet.getBalance(),
                wallet.getCurrency(),
                wallet.getIsActive(),
                wallet.getCreatedAt()
        );
    }

    private TransactionResponseDto mapToTransactionResponseDto(WalletTransaction transaction) {
        return new TransactionResponseDto(
                transaction.getId(),
                transaction.getWalletId(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getDescription(),
                transaction.getReferenceId(),
                transaction.getStatus(),
                transaction.getCreatedAt()
        );
    }
}
