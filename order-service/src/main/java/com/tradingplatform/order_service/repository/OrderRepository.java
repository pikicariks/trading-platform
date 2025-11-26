package com.tradingplatform.order_service.repository;

import com.tradingplatform.order_service.model.Order;
import com.tradingplatform.order_service.model.OrderStatus;
import com.tradingplatform.order_service.model.OrderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    List<Order> findBySymbolOrderByCreatedAtDesc(String symbol);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByStatusIn(List<OrderStatus> statuses);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.userId = :userId AND o.orderType = :orderType AND o.status = 'EXECUTED'")
    BigDecimal getTotalAmountByUserAndType(@Param("userId") Long userId, @Param("orderType") OrderType orderType);

    List<Order> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, OrderStatus status);
}
