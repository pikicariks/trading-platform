package com.tradingplatform.market_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockDetailsResponseDto implements Serializable {

    private String symbol;
    private String companyName;
    private String exchange;
    private String sector;
    private String industry;
    private Long marketCap;
    private BigDecimal peRatio;
    private BigDecimal dividendYield;
    private BigDecimal week52High;
    private BigDecimal week52Low;
    private String description;
}
