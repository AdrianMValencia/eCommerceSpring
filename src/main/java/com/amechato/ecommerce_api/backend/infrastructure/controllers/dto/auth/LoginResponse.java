package com.amechato.ecommerce_api.backend.infrastructure.controllers.dto.auth;

public record LoginResponse(
        String token,
        Integer userId,
        String email,
        String userType) {
}
