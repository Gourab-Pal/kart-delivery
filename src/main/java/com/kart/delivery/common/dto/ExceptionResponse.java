package com.kart.delivery.common.dto;

import java.time.OffsetDateTime;

public record ExceptionResponse(
        String message,
        OffsetDateTime timestamp
) {
}
