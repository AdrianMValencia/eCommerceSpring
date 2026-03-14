package com.amechato.ecommerce_api.backend.infrastructure.config;

import com.amechato.ecommerce_api.backend.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter _jwtAuthenticationFilter;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter) {
        _jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // La API es stateless y se consume con autenticacion por token desde el frontend.
                .csrf(csrf -> csrf.disable())
                // Modo JWT: no se usa sesion HTTP del lado del servidor.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                    // Las peticiones preflight CORS del navegador deben pasar sin token.
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Endpoint publico de login.
                        .requestMatchers("/api/auth/login").permitAll()
                        // Endpoint publico de registro de usuario.
                        .requestMatchers("/api/users").permitAll()
                        // Regla de negocio: las operaciones de ordenes son solo para ADMIN.
                        .requestMatchers("/api/orders/**").hasRole("ADMIN")
                        // Regla de negocio: las operaciones de pago PayPal son solo para ADMIN.
                        .requestMatchers("/api/payments/paypal/**").hasRole("ADMIN")
                        .requestMatchers("/error").permitAll()
                        // Cualquier otra ruta requiere un JWT valido y autenticado.
                        .anyRequest().authenticated())
                // Resolver y autenticar el JWT antes de las validaciones de autorizacion de Spring.
                .addFilterBefore(_jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
