package com.amechato.ecommerce_api.backend.infrastructure.errors;

public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }
}
