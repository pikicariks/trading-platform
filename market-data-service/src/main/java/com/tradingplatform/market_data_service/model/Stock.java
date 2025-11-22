package com.tradingplatform.market_data_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String symbol;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(length = 50)
    private String exchange;

    @Column(length = 100)
    private String sector;

    @Column(length = 100)
    private String industry;

    @Column(name = "current_price", precision = 15, scale = 4)
    private BigDecimal currentPrice;

    @Column(name = "previous_close", precision = 15, scale = 4)
    private BigDecimal previousClose;

    @Column(name = "day_high", precision = 15, scale = 4)
    private BigDecimal dayHigh;

    @Column(name = "day_low", precision = 15, scale = 4)
    private BigDecimal dayLow;

    @Column(name = "open_price", precision = 15, scale = 4)
    private BigDecimal openPrice;

    @Column
    private Long volume;

    @Column(name = "market_cap")
    private Long marketCap;

    @Column(name = "pe_ratio", precision = 10, scale = 2)
    private BigDecimal peRatio;

    @Column(name = "dividend_yield", precision = 10, scale = 4)
    private BigDecimal dividendYield;

    @Column(name = "week_52_high", precision = 15, scale = 4)
    private BigDecimal week52High;

    @Column(name = "week_52_low", precision = 15, scale = 4)
    private BigDecimal week52Low;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
