package com.amechato.ecommerce_api.backend.infrastructure.paypal;

import com.amechato.ecommerce_api.backend.infrastructure.config.PayPalProperties;
import com.amechato.ecommerce_api.backend.infrastructure.errors.PayPalApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PayPalApiClient {

    private final HttpClient _httpClient;
    private final ObjectMapper _objectMapper;
    private final PayPalProperties _payPalProperties;

    public PayPalApiClient(ObjectMapper objectMapper, PayPalProperties payPalProperties) {
        _objectMapper = objectMapper;
        _payPalProperties = payPalProperties;
        _httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public JsonNode createOrder(Map<String, Object> payload) {
        // Paso A: obtener el token OAuth y llamar al endpoint de creacion de orden en PayPal.
        String token = getAccessToken();
        return sendJsonRequest("/v2/checkout/orders", "POST", toJson(payload), token);
    }

    public JsonNode captureOrder(String paypalOrderId) {
        // Paso B: capturar una orden PayPal que ya fue aprobada.
        String token = getAccessToken();
        return sendJsonRequest("/v2/checkout/orders/" + paypalOrderId + "/capture", "POST", "{}", token);
    }

    public boolean verifyWebhookSignature(JsonNode webhookEvent, Map<String, String> headers) {
        // Paso C: pedir a PayPal que verifique la firma del webhook antes de confiar en el evento.
        String token = getAccessToken();

        Map<String, Object> verifyPayload = Map.of(
                "transmission_id", requiredHeader(headers, "PAYPAL-TRANSMISSION-ID"),
                "transmission_time", requiredHeader(headers, "PAYPAL-TRANSMISSION-TIME"),
                "cert_url", requiredHeader(headers, "PAYPAL-CERT-URL"),
                "auth_algo", requiredHeader(headers, "PAYPAL-AUTH-ALGO"),
                "transmission_sig", requiredHeader(headers, "PAYPAL-TRANSMISSION-SIG"),
                "webhook_id", _payPalProperties.getWebhookId(),
                "webhook_event", webhookEvent);

        JsonNode response = sendJsonRequest(
                "/v1/notifications/verify-webhook-signature",
                "POST",
                toJson(verifyPayload),
                token);

        String verificationStatus = response.path("verification_status").asText("");
        return "SUCCESS".equalsIgnoreCase(verificationStatus);
    }

    private String getAccessToken() {
        // Flujo OAuth2 client_credentials de PayPal.
        String credentials = _payPalProperties.getClientId() + ":" + _payPalProperties.getClientSecret();
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        String formBody = "grant_type=" + URLEncoder.encode("client_credentials", StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_payPalProperties.getBaseUrl() + "/v1/oauth2/token"))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Basic " + encodedCredentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        try {
            HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new PayPalApiException("Could not get PayPal access token. Status: " + response.statusCode());
            }

            JsonNode responseJson = _objectMapper.readTree(response.body());
            String token = responseJson.path("access_token").asText(null);
            if (token == null || token.isBlank()) {
                throw new PayPalApiException("PayPal access token is empty");
            }

            return token;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new PayPalApiException("Error calling PayPal token API", ex);
        } catch (IOException ex) {
            throw new PayPalApiException("Error calling PayPal token API", ex);
        }
    }

    private JsonNode sendJsonRequest(String path, String method, String body, String bearerToken) {
        // Llamador JSON generico usado por create/capture/verify-webhook.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_payPalProperties.getBaseUrl() + path))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + bearerToken)
                .header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new PayPalApiException("PayPal API error. Status: " + response.statusCode() + " Body: " + response.body());
            }

            return _objectMapper.readTree(response.body());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new PayPalApiException("Error calling PayPal API", ex);
        } catch (IOException ex) {
            throw new PayPalApiException("Error calling PayPal API", ex);
        }
    }

    private String requiredHeader(Map<String, String> headers, String key) {
        String value = headers.get(key);
        if (value == null || value.isBlank()) {
            throw new PayPalApiException("Missing PayPal webhook header: " + key);
        }

        return value;
    }

    private String toJson(Object payload) {
        try {
            return _objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new PayPalApiException("Could not serialize PayPal payload", ex);
        }
    }
}
