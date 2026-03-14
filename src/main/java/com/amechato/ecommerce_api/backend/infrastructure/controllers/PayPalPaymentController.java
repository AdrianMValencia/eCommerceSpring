package com.amechato.ecommerce_api.backend.infrastructure.controllers;

import com.amechato.ecommerce_api.backend.application.usecases.PayPalCheckoutService;
import com.amechato.ecommerce_api.backend.infrastructure.controllers.dto.payment.CapturePaypalPaymentRequest;
import com.amechato.ecommerce_api.backend.infrastructure.controllers.dto.payment.CapturePaypalPaymentResponse;
import com.amechato.ecommerce_api.backend.infrastructure.controllers.dto.payment.CreatePaypalPaymentRequest;
import com.amechato.ecommerce_api.backend.infrastructure.controllers.dto.payment.CreatePaypalPaymentResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/paypal")
public class PayPalPaymentController {

    private final PayPalCheckoutService _payPalCheckoutService;

    public PayPalPaymentController(PayPalCheckoutService payPalCheckoutService) {
        _payPalCheckoutService = payPalCheckoutService;
    }

    @PostMapping("/create")
    public ResponseEntity<CreatePaypalPaymentResponse> createPayment(@RequestBody CreatePaypalPaymentRequest request) {
        CreatePaypalPaymentResponse response = _payPalCheckoutService.createPayment(request.orderId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/capture")
    public ResponseEntity<CapturePaypalPaymentResponse> capturePayment(@RequestBody CapturePaypalPaymentRequest request) {
        CapturePaypalPaymentResponse response = _payPalCheckoutService.capturePayment(request.orderId(), request.paypalOrderId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> processWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "PAYPAL-TRANSMISSION-ID", required = false) String transmissionId,
            @RequestHeader(value = "PAYPAL-TRANSMISSION-TIME", required = false) String transmissionTime,
            @RequestHeader(value = "PAYPAL-TRANSMISSION-SIG", required = false) String transmissionSig,
            @RequestHeader(value = "PAYPAL-CERT-URL", required = false) String certUrl,
            @RequestHeader(value = "PAYPAL-AUTH-ALGO", required = false) String authAlgo) {

        Map<String, String> headers = new HashMap<>();
        headers.put("PAYPAL-TRANSMISSION-ID", transmissionId);
        headers.put("PAYPAL-TRANSMISSION-TIME", transmissionTime);
        headers.put("PAYPAL-TRANSMISSION-SIG", transmissionSig);
        headers.put("PAYPAL-CERT-URL", certUrl);
        headers.put("PAYPAL-AUTH-ALGO", authAlgo);

        _payPalCheckoutService.processWebhook(payload, headers);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
