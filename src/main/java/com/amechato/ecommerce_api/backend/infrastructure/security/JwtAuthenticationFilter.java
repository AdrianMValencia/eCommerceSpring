package com.amechato.ecommerce_api.backend.infrastructure.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService _jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        _jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Paso FILTER-1: leer el header Authorization y continuar si no existe bearer token.
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Paso FILTER-2: extraer el JWT despues de "Bearer ".
        String token = authHeader.substring(7);
        if (!_jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Paso FILTER-3: extraer la identidad y el rol desde los claims del token firmado.
        Claims claims = _jwtService.extractClaims(token);
        String subject = claims.getSubject();
        String role = claims.get("role", String.class);

        // Paso FILTER-4: construir el contexto de autenticacion de Spring Security.
        if (subject != null && role != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    subject,
                    null,
                    List.of(authority));
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Paso FILTER-5: continuar la peticion hacia la capa de autorizacion/controller.
        filterChain.doFilter(request, response);
    }
}
