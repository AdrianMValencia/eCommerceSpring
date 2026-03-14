package com.amechato.ecommerce_api.backend.infrastructure.errors;

public class WebhookVerificationException extends RuntimeException {

    public WebhookVerificationException(String message) {
        super(message);
    }
}
