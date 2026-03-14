package com.amechato.ecommerce_api.backend.application.usecases;

import com.amechato.ecommerce_api.backend.domain.models.Order;
import com.amechato.ecommerce_api.backend.domain.models.OrderState;
import com.amechato.ecommerce_api.backend.infrastructure.config.PayPalProperties;
import com.amechato.ecommerce_api.backend.infrastructure.controllers.dto.payment.CapturePaypalPaymentResponse;
import com.amechato.ecommerce_api.backend.infrastructure.controllers.dto.payment.CreatePaypalPaymentResponse;
import com.amechato.ecommerce_api.backend.infrastructure.errors.OrderNotFoundException;
import com.amechato.ecommerce_api.backend.infrastructure.errors.PaymentValidationException;
import com.amechato.ecommerce_api.backend.infrastructure.errors.WebhookVerificationException;
import com.amechato.ecommerce_api.backend.infrastructure.paypal.PayPalApiClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PayPalCheckoutService {

    private final OrderService _orderService;
    private final PayPalApiClient _payPalApiClient;
    private final PayPalProperties _payPalProperties;
    private final ObjectMapper _objectMapper;

    public PayPalCheckoutService(
            OrderService orderService,
            PayPalApiClient payPalApiClient,
            PayPalProperties payPalProperties,
            ObjectMapper objectMapper) {
        _orderService = orderService;
        _payPalApiClient = payPalApiClient;
        _payPalProperties = payPalProperties;
        _objectMapper = objectMapper;
    }

    public CreatePaypalPaymentResponse createPayment(Integer orderId) {
        // Paso 1: validar el identificador de orden recibido desde el frontend.
        if (orderId == null || orderId <= 0) {
            throw new PaymentValidationException("Order id is required");
        }

        // Paso 2: cargar y validar la orden local antes de llamar a PayPal.
        Order order = findOrderOrThrow(orderId);
        validateOrderBeforeCreate(order);

        // Paso 3: PayPal exige 2 decimales para monedas como USD.
        String amountValue = order.getTotalOrderPrice()
            .setScale(2, RoundingMode.HALF_UP)
            .toPlainString();

        // Paso 4: construir el payload para la API Orders de PayPal.
        // return_url / cancel_url son rutas del frontend configuradas en properties.
        Map<String, Object> payload = Map.of(
                "intent", "CAPTURE",
                "purchase_units", List.of(Map.of(
                        "reference_id", String.valueOf(order.getId()),
                        "amount", Map.of(
                                "currency_code", _payPalProperties.getCurrency(),
                    "value", amountValue))),
                "application_context", Map.of(
                        "return_url", _payPalProperties.getReturnUrl(),
                        "cancel_url", _payPalProperties.getCancelUrl(),
                        "shipping_preference", "NO_SHIPPING",
                        "user_action", "PAY_NOW"));

                // Paso 5: crear la orden en PayPal y extraer la approval URL para redirigir al frontend.
        JsonNode response = _payPalApiClient.createOrder(payload);
        String paypalOrderId = response.path("id").asText(null);
        String status = response.path("status").asText("CREATED");
        String approvalUrl = extractApprovalUrl(response);

        if (paypalOrderId == null || approvalUrl == null) {
            throw new PaymentValidationException("PayPal order response is invalid");
        }

        // Paso 6: persistir los datos de correlacion en la base local.
        // Esto vincula la orden local <-> la orden PayPal para captura y webhook.
        order.setOrderState(OrderState.PENDING_PAYMENT);
        order.setPaymentProvider("PAYPAL");
        order.setPaymentStatus(status);
        order.setPaypalOrderId(paypalOrderId);
        _orderService.save(order);

        return new CreatePaypalPaymentResponse(order.getId(), paypalOrderId, approvalUrl, status);
    }

    public CapturePaypalPaymentResponse capturePayment(Integer orderId, String paypalOrderId) {
        // Paso 7: validar el payload recibido desde el callback success del frontend.
        if (orderId == null || orderId <= 0) {
            throw new PaymentValidationException("Order id is required");
        }

        Order order = findOrderOrThrow(orderId);

        // Paso 8: guarda de idempotencia: si ya esta pagada, devolver el estado actual.
        if (order.getOrderState() == OrderState.PAID) {
            return new CapturePaypalPaymentResponse(
                    order.getId(),
                    order.getPaypalOrderId(),
                    order.getPaypalCaptureId(),
                    order.getPaymentStatus());
        }

        String orderIdToCapture = paypalOrderId != null && !paypalOrderId.isBlank()
                ? paypalOrderId
                : order.getPaypalOrderId();

        if (orderIdToCapture == null || orderIdToCapture.isBlank()) {
            throw new PaymentValidationException("paypalOrderId is required for capture");
        }

        if (order.getPaypalOrderId() != null && !order.getPaypalOrderId().equals(orderIdToCapture)) {
            throw new PaymentValidationException("paypalOrderId does not match order id");
        }

        // Paso 9: capturar la orden PayPal ya aprobada.
        JsonNode response = _payPalApiClient.captureOrder(orderIdToCapture);
        String status = response.path("status").asText("UNKNOWN");
        String captureId = response
                .path("purchase_units")
                .path(0)
                .path("payments")
                .path("captures")
                .path(0)
                .path("id")
                .asText(null);

        order.setPaymentProvider("PAYPAL");
        order.setPaymentStatus(status);
        order.setPaypalOrderId(orderIdToCapture);
        order.setPaypalCaptureId(captureId);

        // Paso 10: mapear el estado de PayPal al estado local de la orden.
        if ("COMPLETED".equalsIgnoreCase(status)) {
            order.setOrderState(OrderState.PAID);
            order.setPaidAt(LocalDateTime.now());
        } else {
            order.setOrderState(OrderState.PAYMENT_FAILED);
        }

        _orderService.save(order);

        return new CapturePaypalPaymentResponse(order.getId(), orderIdToCapture, captureId, status);
    }

    public void processWebhook(String payload, Map<String, String> headers) {
        // Paso 11: parsear el webhook y verificar la firma contra la API de PayPal.
        // Nota: en local se necesita ngrok para que PayPal pueda alcanzar este endpoint.
        JsonNode event = parsePayload(payload);
        boolean verified = _payPalApiClient.verifyWebhookSignature(event, headers);
        if (!verified) {
            throw new WebhookVerificationException("Invalid PayPal webhook signature");
        }

        // Paso 12: resolver la orden local por paypalOrderId y aplicar el estado del evento.
        String eventType = event.path("event_type").asText("");
        String paypalOrderId = extractOrderIdFromWebhook(event);

        if (paypalOrderId == null || paypalOrderId.isBlank()) {
            return;
        }

        Order order = _orderService.findByPaypalOrderId(paypalOrderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found for PayPal order id " + paypalOrderId));

        if ("PAYMENT.CAPTURE.COMPLETED".equals(eventType)) {
            String captureId = event.path("resource").path("id").asText(null);
            order.setOrderState(OrderState.PAID);
            order.setPaymentProvider("PAYPAL");
            order.setPaymentStatus("COMPLETED");
            order.setPaypalCaptureId(captureId);
            order.setPaidAt(LocalDateTime.now());
            _orderService.save(order);
            return;
        }

        if ("PAYMENT.CAPTURE.DENIED".equals(eventType)
                || "PAYMENT.CAPTURE.DECLINED".equals(eventType)
                || "PAYMENT.CAPTURE.REFUNDED".equals(eventType)) {
            order.setOrderState(OrderState.PAYMENT_FAILED);
            order.setPaymentProvider("PAYPAL");
            order.setPaymentStatus(eventType);
            _orderService.save(order);
            return;
        }

        if ("CHECKOUT.ORDER.APPROVED".equals(eventType) && order.getOrderState() == OrderState.CREATED) {
            order.setOrderState(OrderState.PENDING_PAYMENT);
            order.setPaymentProvider("PAYPAL");
            order.setPaymentStatus("APPROVED");
            _orderService.save(order);
        }
    }

    private void validateOrderBeforeCreate(Order order) {
        if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            throw new PaymentValidationException("Order has no items");
        }

        if (order.getTotalOrderPrice() == null || order.getTotalOrderPrice().signum() <= 0) {
            throw new PaymentValidationException("Order total must be greater than zero");
        }

        if (order.getOrderState() != null
                && order.getOrderState() != OrderState.CREATED
                && order.getOrderState() != OrderState.PENDING_PAYMENT
                && order.getOrderState() != OrderState.PAYMENT_FAILED) {
            throw new PaymentValidationException("Order state does not allow payment creation");
        }
    }

    private Order findOrderOrThrow(Integer orderId) {
        try {
            return _orderService.findById(orderId);
        } catch (RuntimeException ex) {
            throw new OrderNotFoundException("Order not found with id: " + orderId);
        }
    }

    private JsonNode parsePayload(String payload) {
        try {
            return _objectMapper.readTree(payload);
        } catch (JsonProcessingException ex) {
            throw new PaymentValidationException("Invalid webhook payload");
        }
    }

    private String extractApprovalUrl(JsonNode response) {
        JsonNode links = response.path("links");
        if (!links.isArray()) {
            return null;
        }

        for (JsonNode link : links) {
            if ("approve".equalsIgnoreCase(link.path("rel").asText())) {
                return link.path("href").asText(null);
            }
        }

        return null;
    }

    private String extractOrderIdFromWebhook(JsonNode event) {
        JsonNode resource = event.path("resource");
        String eventType = event.path("event_type").asText("");

        if ("CHECKOUT.ORDER.APPROVED".equals(eventType)) {
            return resource.path("id").asText(null);
        }

        String relatedOrderId = resource
                .path("supplementary_data")
                .path("related_ids")
                .path("order_id")
                .asText(null);

        if (relatedOrderId != null && !relatedOrderId.isBlank()) {
            return relatedOrderId;
        }

        return resource.path("invoice_id").asText(null);
    }
}
