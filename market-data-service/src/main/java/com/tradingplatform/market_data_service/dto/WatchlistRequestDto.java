package com.tradingplatform.market_data_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistRequestDto {

    @NotNull(message = "User Id is required")
    private Long userId;

    @NotBlank(message = "Stock symbol is required")
    private String symbol;

    private String notes;
}
