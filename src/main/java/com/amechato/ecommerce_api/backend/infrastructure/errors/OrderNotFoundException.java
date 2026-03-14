package com.amechato.ecommerce_api.backend.infrastructure.errors;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String message) {
        super(message);
    }
}
