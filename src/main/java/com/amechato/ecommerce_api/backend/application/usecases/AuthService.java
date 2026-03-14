package com.amechato.ecommerce_api.backend.application.usecases;

import com.amechato.ecommerce_api.backend.domain.models.User;
import com.amechato.ecommerce_api.backend.infrastructure.controllers.dto.auth.LoginResponse;
import com.amechato.ecommerce_api.backend.infrastructure.errors.AuthException;
import com.amechato.ecommerce_api.backend.infrastructure.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService _userService;
    private final PasswordEncoder _passwordEncoder;
    private final JwtService _jwtService;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        _userService = userService;
        _passwordEncoder = passwordEncoder;
        _jwtService = jwtService;
    }

    public LoginResponse login(String email, String password) {
        // Paso LOGIN-1: validar el payload obligatorio de credenciales.
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new AuthException("Email and password are required");
        }

        // Paso LOGIN-2: cargar el usuario por email desde el repositorio local.
        User user = _userService.findByEmail(email);
        if (user == null || user.getPassword() == null) {
            throw new AuthException("Invalid credentials");
        }

        // Paso LOGIN-3: comparar la contrasena en texto plano contra el hash BCrypt almacenado en BD.
        if (!_passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthException("Invalid credentials");
        }

        // Paso LOGIN-4: generar el JWT y devolver token + rol para autorizacion en frontend.
        String token = _jwtService.generateToken(user);
        return new LoginResponse(token, user.getId(), user.getEmail(), user.getUserType().name());
    }
}
