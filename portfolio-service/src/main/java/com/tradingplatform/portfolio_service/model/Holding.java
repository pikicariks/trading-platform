package com.tradingplatform.portfolio_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "holdings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"portfolio_id", "symbol"})
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "average_price", nullable = false, precision = 15, scale = 4)
    private BigDecimal averagePrice = BigDecimal.ZERO;

    @Column(name = "total_invested", precision = 15, scale = 2)
    private BigDecimal totalInvested = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
