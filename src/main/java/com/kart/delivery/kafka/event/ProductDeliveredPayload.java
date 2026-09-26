package com.kart.delivery.kafka.event;

import java.util.UUID;

public record ProductDeliveredPayload(
        UUID deliveryId,
        UUID orderId
) {
}
