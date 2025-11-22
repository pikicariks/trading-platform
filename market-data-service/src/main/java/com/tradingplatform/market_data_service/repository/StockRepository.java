package com.tradingplatform.market_data_service.repository;

import com.tradingplatform.market_data_service.model.Stock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends CrudRepository<Stock, Long> {

    Optional<Stock> findBySymbol(String symbol);

    boolean existsBySymbol(String symbol);

    List<Stock> findBySymbolContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(
            String symbol, String companyName);

    List<Stock> findBySectorIgnoreCase(String sector);
}
