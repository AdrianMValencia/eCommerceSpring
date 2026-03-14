package com.amechato.ecommerce_api.backend.infrastructure.controllers.dto.payment;

public record CapturePaypalPaymentRequest(
        Integer orderId,
        String paypalOrderId) {
}
