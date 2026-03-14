package com.amechato.ecommerce_api.backend.infrastructure.errors;

public class PayPalApiException extends RuntimeException {

    public PayPalApiException(String message) {
        super(message);
    }

    public PayPalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
