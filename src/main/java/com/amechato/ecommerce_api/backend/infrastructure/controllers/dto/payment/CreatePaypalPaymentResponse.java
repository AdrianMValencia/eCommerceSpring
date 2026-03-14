package com.amechato.ecommerce_api.backend.infrastructure.controllers.dto.payment;

public record CreatePaypalPaymentResponse(
        Integer orderId,
        String paypalOrderId,
        String approvalUrl,
        String status) {
}
