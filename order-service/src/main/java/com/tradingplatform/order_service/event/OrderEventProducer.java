package com.tradingplatform.order_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private static final String TOPIC = "order-events";

    public void publishOrderEvent(OrderEvent event) {
        log.info("Publishing order event: {}", event);
        kafkaTemplate.send(TOPIC, event.getOrderId().toString(), event);
    }
}
