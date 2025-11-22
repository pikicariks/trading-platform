package com.tradingplatform.market_data_service.repository;

import com.tradingplatform.market_data_service.model.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    List<Watchlist> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Watchlist> findByUserIdAndSymbol(Long userId, String symbol);

    boolean existsByUserIdAndSymbol(Long userId, String symbol);

    void deleteByUserIdAndSymbol(Long userId, String symbol);

    long countByUserId(Long userId);
}
