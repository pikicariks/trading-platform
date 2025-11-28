package com.tradingplatform.portfolio_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "portfolios")
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "total_value", precision = 15, scale = 2)
    private BigDecimal totalValue = BigDecimal.ZERO;

    @Column(name = "cash_balance", precision = 15, scale = 2)
    private BigDecimal cashBalance = BigDecimal.ZERO;

    @Column(name = "invested_amount", precision = 15, scale = 2)
    private BigDecimal investedAmount = BigDecimal.ZERO;

    @Column(name = "total_profit_loss", precision = 15, scale = 2)
    private BigDecimal totalProfitLoss = BigDecimal.ZERO;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Holding> holdings = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


}
