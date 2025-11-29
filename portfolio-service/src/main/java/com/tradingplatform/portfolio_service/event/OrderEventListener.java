package com.tradingplatform.portfolio_service.event;

import com.tradingplatform.portfolio_service.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventListener {

    @Autowired
    private PortfolioService portfolioService;

    @KafkaListener(topics = "order-events", groupId = "portfolio-service-group")
    public void handleOrderEvent(OrderEvent event, Acknowledgment acknowledgment) {
        log.info("Received order event: {}", event);

        try {
            if ("ORDER_EXECUTED".equals(event.getEventType())) {
                processOrderExecuted(event);
                acknowledgment.acknowledge();
                log.info("Successfully processed order event: {}", event.getOrderId());
            } else {
                log.warn("Unknown event type: {}", event.getEventType());
                acknowledgment.acknowledge();
            }
        } catch (Exception e) {
            log.error("Failed to process order event {}: {}", event.getOrderId(), e.getMessage(), e);
        }
    }

    private void processOrderExecuted(OrderEvent event) {
        if ("BUY".equals(event.getOrderType())) {
            portfolioService.processBuyOrder(
                    event.getUserId(),
                    event.getSymbol(),
                    event.getQuantity(),
                    event.getPricePerShare()
            );
        } else if ("SELL".equals(event.getOrderType())) {
            portfolioService.processSellOrder(
                    event.getUserId(),
                    event.getSymbol(),
                    event.getQuantity(),
                    event.getPricePerShare()
            );
        } else {
            log.warn("Unknown order type: {}", event.getOrderType());
        }
    }
}
