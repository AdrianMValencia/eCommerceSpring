package com.amechato.ecommerce_api.backend.infrastructure.errors;

import java.time.Instant;

public record ApiErrorResponse(
        String code,
        String message,
        Instant timestamp,
        String path) {
}
