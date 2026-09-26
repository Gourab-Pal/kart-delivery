package com.kart.delivery.delivery.service;

import com.kart.delivery.delivery.dto.DeliveryResponse;
import com.kart.delivery.delivery.dto.DeliveryStatusUpdateRequest;
import com.kart.delivery.delivery.entity.DeliveryEntity;
import com.kart.delivery.delivery.exception.OrderDetailsNotFoundException;
import com.kart.delivery.delivery.repository.DeliveryRepository;
import com.kart.delivery.kafka.event.ProductDeliveredPayload;
import com.kart.delivery.outbox.service.OutboxEventService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OutboxEventService outboxEventService;

    public DeliveryService(
            DeliveryRepository deliveryRepository,
            OutboxEventService outboxEventService
    ) {
        this.deliveryRepository = deliveryRepository;
        this.outboxEventService = outboxEventService;
    }

    @Transactional
    public void createDeliveryRecord(UUID orderId) {
        if(deliveryRepository.existsByOrderId(orderId)) {
            return;
        }
        DeliveryEntity deliveryEntity = new DeliveryEntity(orderId);
        deliveryRepository.save(deliveryEntity);
    }

    @Transactional
    public DeliveryResponse fetchDeliveryRecord(UUID orderId) {
        if(!deliveryRepository.existsByOrderId(orderId)) {
            throw new OrderDetailsNotFoundException(orderId);
        }

        DeliveryEntity deliveryEntity = deliveryRepository.findByOrderId(orderId).orElseThrow(() -> new OrderDetailsNotFoundException(orderId));
        return DeliveryResponse.from(deliveryEntity);
    }

    @Transactional
    public DeliveryResponse updateDeliveryStatus(DeliveryStatusUpdateRequest request) {
        if(!deliveryRepository.existsByOrderId(request.orderId())) {
            throw new OrderDetailsNotFoundException(request.orderId());
        }

        DeliveryEntity deliveryEntity = deliveryRepository.findByOrderId(request.orderId()).orElseThrow(() -> new OrderDetailsNotFoundException(request.orderId()));
        deliveryEntity.updateStatus(request.status(), request.trackingNumber());
        if(request.status().toString().equals("DELIVERED")) {
            outboxEventService.saveEvent(
                    "delivery",
                    deliveryEntity.getId(),
                    "PRODUCTS_DELIVERED",
                    1,
                    new ProductDeliveredPayload(deliveryEntity.getId(), request.orderId())
            );
        }
        return DeliveryResponse.from(deliveryEntity);
    }

    @Transactional
    public void cancelDeliveryRecord(UUID orderId) {
        DeliveryEntity deliveryEntity = deliveryRepository.findByOrderId(orderId).orElseThrow(() -> new OrderDetailsNotFoundException(orderId));
        deliveryEntity.updateStatus(DeliveryEntity.DeliveryStatus.CANCELLED, null);
    }
}
