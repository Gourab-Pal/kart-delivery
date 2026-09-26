package com.kart.delivery.delivery.exception;

import java.util.UUID;

public class OrderDetailsNotFoundException extends RuntimeException {
    public OrderDetailsNotFoundException(UUID orderId) {
        super("Could not find order details for orderId: " + orderId + " in delivery table");
    }
}
