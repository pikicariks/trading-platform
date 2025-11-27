package com.tradingplatform.order_service.controller;

import com.tradingplatform.order_service.dto.CreateOrderRequestDto;
import com.tradingplatform.order_service.dto.OrderResponseDto;
import com.tradingplatform.order_service.dto.OrderSummaryResponseDto;
import com.tradingplatform.order_service.model.OrderStatus;
import com.tradingplatform.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Order Service is running!");
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody CreateOrderRequestDto request) {
        OrderResponseDto response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable("orderId") Long orderId) {
        OrderResponseDto response = orderService.getOrderById(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDto>> getUserOrders(@PathVariable("userId") Long userId) {
        List<OrderResponseDto> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/user/{userId}/paged")
    public ResponseEntity<Page<OrderResponseDto>> getUserOrdersPaged(
            @PathVariable("userId") Long userId,
            Pageable pageable
    ) {
        Page<OrderResponseDto> orders = orderService.getUserOrdersPaged(userId, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<OrderResponseDto>> getUserOrdersByStatus(
            @PathVariable("userId") Long userId,
            @PathVariable("status") OrderStatus status
    ) {
        List<OrderResponseDto> orders = orderService.getUserOrdersByStatus(userId, status);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDto> cancelOrder(
            @PathVariable("orderId") Long orderId,
            @RequestParam("userId") Long userId
    ) {
        OrderResponseDto response = orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<OrderSummaryResponseDto> getUserOrderSummary(@PathVariable("userId") Long userId) {
        OrderSummaryResponseDto summary = orderService.getUserOrderSummary(userId);
        return ResponseEntity.ok(summary);
    }
}
