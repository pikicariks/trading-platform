package com.tradingplatform.order_service.event;

import com.tradingplatform.order_service.model.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {

    private Long orderId;
    private Long userId;
    private String symbol;
    private OrderType orderType;
    private Integer quantity;
    private BigDecimal pricePerShare;
    private BigDecimal totalAmount;
    private LocalDateTime executedAt;
    private String eventType;  // ORDER_CREATED, ORDER_EXECUTED, ORDER_FAILED

    public static OrderEvent orderExecuted(Long orderId, Long userId, String symbol,
                                           OrderType orderType, Integer quantity,
                                           BigDecimal pricePerShare, BigDecimal totalAmount) {
        OrderEvent event = new OrderEvent();
        event.setOrderId(orderId);
        event.setUserId(userId);
        event.setSymbol(symbol);
        event.setOrderType(orderType);
        event.setQuantity(quantity);
        event.setPricePerShare(pricePerShare);
        event.setTotalAmount(totalAmount);
        event.setExecutedAt(LocalDateTime.now());
        event.setEventType("ORDER_EXECUTED");
        return event;
    }
}
