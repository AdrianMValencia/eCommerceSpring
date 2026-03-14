package com.amechato.ecommerce_api.backend.infrastructure.controllers.dto.auth;

public record LoginRequest(
        String email,
        String password) {
}
