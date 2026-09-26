package com.kart.delivery.delivery.service;

import com.kart.delivery.delivery.entity.DeliveryEntity;
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
}
