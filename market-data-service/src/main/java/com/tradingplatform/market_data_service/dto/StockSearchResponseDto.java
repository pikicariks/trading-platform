package com.tradingplatform.market_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockSearchResponseDto {

    private List<StockSearchResult> results;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockSearchResult {
        private String symbol;
        private String name;
        private String type;
        private String region;
        private String currency;
    }
}
