package com.tradingplatform.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(name = "market-data-service")
public interface MarketDataServiceClient {

    @GetMapping("/api/market/price/{symbol}")
    BigDecimal getCurrentPrice(@PathVariable("symbol") String symbol);

    @GetMapping("/api/market/quote/{symbol}")
    StockQuoteResponse getQuote(@PathVariable("symbol") String symbol);

    class StockQuoteResponse {
        public String symbol;
        public String companyName;
        public BigDecimal price;
        public BigDecimal change;
        public BigDecimal changePercent;
    }
}
