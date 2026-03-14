package com.amechato.ecommerce_api.backend.infrastructure.controllers;

import com.amechato.ecommerce_api.backend.application.usecases.AuthService;
import com.amechato.ecommerce_api.backend.infrastructure.controllers.dto.auth.LoginRequest;
import com.amechato.ecommerce_api.backend.infrastructure.controllers.dto.auth.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService _authService;

    public AuthController(AuthService authService) {
        _authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = _authService.login(request.email(), request.password());
        return ResponseEntity.ok(response);
    }
}
