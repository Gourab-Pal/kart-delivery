package com.kart.delivery.delivery.controller;

import com.kart.delivery.delivery.dto.DeliveryResponse;
import com.kart.delivery.delivery.dto.DeliveryStatusUpdateRequest;
import com.kart.delivery.delivery.service.DeliveryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/order/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public DeliveryResponse fetchDeliveryRecord(@PathVariable UUID orderId) {
        return deliveryService.fetchDeliveryRecord(orderId);
    }

    @PostMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public DeliveryResponse updateDeliveryStatus(DeliveryStatusUpdateRequest request) {
        return deliveryService.updateDeliveryStatus(request);
    }
}
