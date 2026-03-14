package com.amechato.ecommerce_api.backend.infrastructure.errors;

public class PaymentValidationException extends RuntimeException {

    public PaymentValidationException(String message) {
        super(message);
    }
}
