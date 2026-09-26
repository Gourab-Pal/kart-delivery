package com.kart.delivery.common.exception;

import com.kart.delivery.common.dto.ExceptionResponse;
import com.kart.delivery.delivery.exception.OrderDetailsNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(OrderDetailsNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleOrderDetailsNotFoundException(OrderDetailsNotFoundException exception) {
        log.error(exception.getMessage());
        return new ExceptionResponse(exception.getMessage(), OffsetDateTime.now());
    }
}
