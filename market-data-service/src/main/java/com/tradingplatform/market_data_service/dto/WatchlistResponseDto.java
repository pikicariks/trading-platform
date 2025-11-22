package com.tradingplatform.market_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistResponseDto {

    private Long id;
    private Long userId;
    private String symbol;
    private String companyName;
    private BigDecimal currentPrice;
    private BigDecimal changePercent;
    private String notes;
    private LocalDateTime addedAt;
}
