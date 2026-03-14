package com.amechato.ecommerce_api.backend.infrastructure.errors;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleOrderNotFound(OrderNotFoundException ex, WebRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(PaymentValidationException.class)
    public ResponseEntity<ApiErrorResponse> handlePaymentValidation(PaymentValidationException ex, WebRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "PAYMENT_VALIDATION_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(PayPalApiException.class)
    public ResponseEntity<ApiErrorResponse> handlePayPalError(PayPalApiException ex, WebRequest request) {
        return buildResponse(HttpStatus.BAD_GATEWAY, "PAYPAL_API_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthError(AuthException ex, WebRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "AUTH_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(WebhookVerificationException.class)
    public ResponseEntity<ApiErrorResponse> handleWebhookVerification(WebhookVerificationException ex, WebRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "WEBHOOK_VERIFICATION_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, WebRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage(), request);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String code, String message, WebRequest request) {
        ApiErrorResponse error = new ApiErrorResponse(
                code,
                message,
                Instant.now(),
                request.getDescription(false));

        return ResponseEntity.status(status).body(error);
    }
}
