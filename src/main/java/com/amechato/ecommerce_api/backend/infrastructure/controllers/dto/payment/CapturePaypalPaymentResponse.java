package com.amechato.ecommerce_api.backend.infrastructure.controllers.dto.payment;

public record CapturePaypalPaymentResponse(
        Integer orderId,
        String paypalOrderId,
        String captureId,
        String status) {
}
