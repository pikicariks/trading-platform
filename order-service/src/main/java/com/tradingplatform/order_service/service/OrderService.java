package com.tradingplatform.order_service.service;

import com.tradingplatform.order_service.dto.CreateOrderRequestDto;
import com.tradingplatform.order_service.dto.OrderResponseDto;
import com.tradingplatform.order_service.dto.OrderSummaryResponseDto;
import com.tradingplatform.order_service.model.Order;
import com.tradingplatform.order_service.model.OrderStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderService {

    @Transactional
    OrderResponseDto createOrder(CreateOrderRequestDto request);

    OrderResponseDto getOrderById(Long orderId);

    List<OrderResponseDto> getUserOrders(Long userId);

    Page<OrderResponseDto> getUserOrdersPaged(Long userId, Pageable pageable);

    List<OrderResponseDto> getUserOrdersByStatus(Long userId, OrderStatus status);

    @Transactional
    OrderResponseDto cancelOrder(Long orderId, Long userId);

    OrderSummaryResponseDto getUserOrderSummary(Long userId);
}
