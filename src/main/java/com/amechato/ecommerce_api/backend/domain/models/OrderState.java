package com.amechato.ecommerce_api.backend.domain.models;

public enum OrderState {
    CREATED,
    PENDING_PAYMENT,
    PAID,
    PAYMENT_FAILED,
    CANCELLED,
    CONFIRMED
}
