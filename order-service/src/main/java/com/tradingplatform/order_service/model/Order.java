package com.tradingplatform.order_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "price_per_share", precision = 15, scale = 4)
    private BigDecimal pricePerShare;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal commission = BigDecimal.ZERO;

    @Column(length = 500)
    private String notes;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;
}
