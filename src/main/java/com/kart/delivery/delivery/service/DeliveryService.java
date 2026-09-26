package com.kart.delivery.delivery.service;

import com.kart.delivery.delivery.dto.DeliveryResponse;
import com.kart.delivery.delivery.dto.DeliveryStatusUpdateRequest;
import com.kart.delivery.delivery.entity.DeliveryEntity;
import com.kart.delivery.delivery.exception.OrderDetailsNotFoundException;
import com.kart.delivery.delivery.repository.DeliveryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    public DeliveryService(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
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
        return DeliveryResponse.from(deliveryEntity);
    }
}
