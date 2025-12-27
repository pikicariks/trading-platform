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
    private double price;
    private double change;
    private double changePercent;
    private double previousClose;
    private double open;
    private double dayHigh;
    private double dayLow;
    private Long volume;
    private LocalDateTime lastUpdated;
}
