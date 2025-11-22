package com.tradingplatform.wallet_service.repository;

import com.tradingplatform.wallet_service.model.TransactionType;
import com.tradingplatform.wallet_service.model.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);

    Page<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);

    List<WalletTransaction> findByWalletIdAndTransactionType(Long walletId, TransactionType type);

    List<WalletTransaction> findByWalletIdAndCreatedAtBetween(
            Long walletId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
