package com.tradingplatform.market_data_service.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradingplatform.market_data_service.dto.StockDetailsResponseDto;
import com.tradingplatform.market_data_service.dto.StockQuoteResponseDto;
import com.tradingplatform.market_data_service.dto.StockSearchResponseDto;
import com.tradingplatform.market_data_service.exception.ApiException;
import com.tradingplatform.market_data_service.exception.StockNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class AlphaVantageClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${alpha-vantage.api-key}")
    private String apiKey;

    public AlphaVantageClient(@Value("${alpha-vantage.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public StockQuoteResponseDto getQuote(String symbol) {
        log.info("Fetching quote for symbol: {}", symbol);

        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("function", "GLOBAL_QUOTE")
                            .queryParam("symbol", symbol)
                            .queryParam("apikey", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);

            if (root.has("Note") || root.has("Information")) {
                log.warn("API limit reached or info message: {}", root.toString());
                throw new ApiException("API rate limit reached. Please try again later.");
            }

            JsonNode quote = root.get("Global Quote");

            if (quote == null || quote.isEmpty()) {
                throw new StockNotFoundException("Stock not found: " + symbol);
            }

            BigDecimal price = parseBigDecimal(quote.get("05. price"));
            BigDecimal previousClose = parseBigDecimal(quote.get("08. previous close"));
            BigDecimal change = parseBigDecimal(quote.get("09. change"));
            BigDecimal changePercent = parsePercentage(quote.get("10. change percent"));

            StockQuoteResponseDto quoteResponse = new StockQuoteResponseDto();
            quoteResponse.setSymbol(symbol.toUpperCase());
            quoteResponse.setPrice(price);
            quoteResponse.setPreviousClose(previousClose);
            quoteResponse.setChange(change);
            quoteResponse.setChangePercent(changePercent);
            quoteResponse.setOpen(parseBigDecimal(quote.get("02. open")));
            quoteResponse.setDayHigh(parseBigDecimal(quote.get("03. high")));
            quoteResponse.setDayLow(parseBigDecimal(quote.get("04. low")));
            quoteResponse.setVolume(parseLong(quote.get("06. volume")));
            quoteResponse.setLastUpdated(LocalDateTime.now());

            return quoteResponse;

        } catch (StockNotFoundException | ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching quote for {}: {}", symbol, e.getMessage());
            throw new ApiException("Failed to fetch stock quote: " + e.getMessage());
        }
    }

    public StockDetailsResponseDto getCompanyOverview(String symbol) {
        log.info("Fetching company overview for symbol: {}", symbol);

        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("function", "OVERVIEW")
                            .queryParam("symbol", symbol)
                            .queryParam("apikey", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);

            if (root.has("Note") || root.has("Information")) {
                throw new ApiException("API rate limit reached. Please try again later.");
            }

            if (!root.has("Symbol") || root.get("Symbol").asText().isEmpty()) {
                throw new StockNotFoundException("Company details not found: " + symbol);
            }

            StockDetailsResponseDto details = new StockDetailsResponseDto();
            details.setSymbol(getTextValue(root, "Symbol"));
            details.setCompanyName(getTextValue(root, "Name"));
            details.setExchange(getTextValue(root, "Exchange"));
            details.setSector(getTextValue(root, "Sector"));
            details.setIndustry(getTextValue(root, "Industry"));
            details.setMarketCap(parseLongValue(root, "MarketCapitalization"));
            details.setPeRatio(parseBigDecimalValue(root, "PERatio"));
            details.setDividendYield(parseBigDecimalValue(root, "DividendYield"));
            details.setWeek52High(parseBigDecimalValue(root, "52WeekHigh"));
            details.setWeek52Low(parseBigDecimalValue(root, "52WeekLow"));
            details.setDescription(getTextValue(root, "Description"));

            return details;

        } catch (StockNotFoundException | ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching company overview for {}: {}", symbol, e.getMessage());
            throw new ApiException("Failed to fetch company details: " + e.getMessage());
        }
    }

    public StockSearchResponseDto searchSymbol(String keywords) {
        log.info("Searching for stocks with keywords: {}", keywords);

        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("function", "SYMBOL_SEARCH")
                            .queryParam("keywords", keywords)
                            .queryParam("apikey", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);

            if (root.has("Note") || root.has("Information")) {
                throw new ApiException("API rate limit reached. Please try again later.");
            }

            JsonNode matches = root.get("bestMatches");
            List<StockSearchResponseDto.StockSearchResult> results = new ArrayList<>();

            if (matches != null && matches.isArray()) {
                for (JsonNode match : matches) {
                    StockSearchResponseDto.StockSearchResult result = new StockSearchResponseDto.StockSearchResult();
                    result.setSymbol(getTextValue(match, "1. symbol"));
                    result.setName(getTextValue(match, "2. name"));
                    result.setType(getTextValue(match, "3. type"));
                    result.setRegion(getTextValue(match, "4. region"));
                    result.setCurrency(getTextValue(match, "8. currency"));
                    results.add(result);
                }
            }

            return new StockSearchResponseDto(results);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error searching stocks: {}", e.getMessage());
            throw new ApiException("Failed to search stocks: " + e.getMessage());
        }
    }

    // Helper methods
    private BigDecimal parseBigDecimal(JsonNode node) {
        if (node == null || node.asText().isEmpty() || node.asText().equals("None")) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(node.asText()).setScale(4, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal parsePercentage(JsonNode node) {
        if (node == null || node.asText().isEmpty()) {
            return BigDecimal.ZERO;
        }
        String value = node.asText().replace("%", "");
        try {
            return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private Long parseLong(JsonNode node) {
        if (node == null || node.asText().isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(node.asText());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private String getTextValue(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.asText().equals("None") || fieldNode.asText().equals("-")) {
            return null;
        }
        return fieldNode.asText();
    }

    private BigDecimal parseBigDecimalValue(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return parseBigDecimal(fieldNode);
    }

    private Long parseLongValue(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return parseLong(fieldNode);
    }
}
