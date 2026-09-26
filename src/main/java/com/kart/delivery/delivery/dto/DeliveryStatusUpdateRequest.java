package com.kart.delivery.delivery.dto;

import com.kart.delivery.delivery.entity.DeliveryEntity;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeliveryStatusUpdateRequest(

        @NotNull
        UUID orderId,

        @NotNull
        DeliveryEntity.DeliveryStatus status
) {
}
