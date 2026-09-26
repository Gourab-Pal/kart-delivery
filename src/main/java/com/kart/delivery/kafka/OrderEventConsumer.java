package com.kart.delivery.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.kart.delivery.delivery.entity.DeliveryEntity;
import com.kart.delivery.delivery.exception.OrderDetailsNotFoundException;
import com.kart.delivery.delivery.repository.DeliveryRepository;
import com.kart.delivery.delivery.service.DeliveryService;
import com.kart.delivery.kafka.event.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventConsumer.class);
    private final DeliveryService deliveryService;
    DeliveryRepository deliveryRepository;

    public OrderEventConsumer(DeliveryService deliveryService,  DeliveryRepository deliveryRepository) {
        this.deliveryService = deliveryService;
        this.deliveryRepository = deliveryRepository;
    }

    @KafkaListener(
            topics = "${kafka.topic.order-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "orderKafkaListenerContainerFactory"
    )
    public void consume(OrderEvent event) {
        switch (event.eventType()) {
            case "ORDER_CONFIRMED" ->
                handleOrderConfirmed(event);
            case "ORDER_CANCELLED" ->
                handleOrderCancelled(event);
            default ->
                throw new IllegalStateException("Unsupported order event type: " + event.eventType());
        }
    }

    private void handleOrderConfirmed(OrderEvent event) {
        JsonNode payload = event.payload();
        UUID orderId = UUID.fromString(payload.get("orderId").asText());
        deliveryService.createDeliveryRecord(orderId);
        logger.info("Delivery record created for orderId: {}", orderId);
    }

    private void handleOrderCancelled(OrderEvent event) {
        JsonNode payload = event.payload();
        UUID orderId = UUID.fromString(payload.get("orderId").asText());
        DeliveryEntity deliveryEntity = deliveryRepository.findByOrderId(orderId).orElseThrow(() -> new OrderDetailsNotFoundException(orderId));
        deliveryEntity.updateStatus(DeliveryEntity.DeliveryStatus.CANCELLED, null);
    }
}
