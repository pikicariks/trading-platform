package com.tradingplatform.market_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockQuoteResponseDto implements Serializable {

    private String symbol;
    private String companyName;
    private BigDecimal price;
    private BigDecimal change;
    private BigDecimal changePercent;
    private BigDecimal previousClose;
    private BigDecimal open;
    private BigDecimal dayHigh;
    private BigDecimal dayLow;
    private Long volume;
    private LocalDateTime lastUpdated;
}
