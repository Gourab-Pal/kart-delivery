package com.kart.delivery.delivery.dto;

import com.kart.delivery.delivery.entity.DeliveryEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DeliveryResponse(
        UUID deliveryId,
        UUID orderId,
        String status,
        String trackingNumber,
        OffsetDateTime pickedUpAt,
        OffsetDateTime deliveredAt,
        OffsetDateTime cancelledAt
) {
    public static DeliveryResponse from(DeliveryEntity deliveryEntity) {
        return new DeliveryResponse(
                deliveryEntity.getId(),
                deliveryEntity.getOrderId(),
                deliveryEntity.getStatus().toString(),
                deliveryEntity.getTrackingNumber(),
                deliveryEntity.getPickedUpAt(),
                deliveryEntity.getDeliveredAt(),
                deliveryEntity.getCancelledAt()
        );
    }
}
