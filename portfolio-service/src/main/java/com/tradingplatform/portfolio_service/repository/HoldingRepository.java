package com.tradingplatform.portfolio_service.repository;

import com.tradingplatform.portfolio_service.model.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding,Long> {

    List<Holding> findByPortfolioId(Long portfolioId);

    Optional<Holding> findByPortfolioIdAndSymbol(Long portfolioId, String symbol);

    @Query("SELECT h FROM Holding h WHERE h.portfolio.userId = :userId")
    List<Holding> findByUserId(@Param("userId") Long userId);

    @Query("SELECT h FROM Holding h WHERE h.portfolio.userId = :userId AND h.symbol = :symbol")
    Optional<Holding> findByUserIdAndSymbol(@Param("userId") Long userId, @Param("symbol") String symbol);

    void deleteByPortfolioIdAndSymbol(Long portfolioId, String symbol);
}
