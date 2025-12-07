package com.example.front_office.config;

import com.example.front_office.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j // Para logs (opcional, requiere Lombok)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;      // Clientes
    private final UserDetailsService adminUserDetailsService; // Back Office

    public JwtAuthenticationFilter(
            JwtService jwtService,
            @Qualifier("userDetailsService") UserDetailsService userDetailsService,
            @Qualifier("adminUserDetailsService") UserDetailsService adminUserDetailsService
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.adminUserDetailsService = adminUserDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Validar cabecera
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            userEmail = jwtService.extractUsername(jwt); // Puede lanzar error si el token está corrupto
        } catch (Exception e) {
            log.error("Error al extraer username del token: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Si hay email y no está autenticado todavía
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = null;
            boolean tokenValido = false;

            // ------------------------------------------------------------------
            // ESTRATEGIA: INTENTO 1 - BUSCAR EN CLIENTES (USUARIOS)
            // ------------------------------------------------------------------
            // Nota: Si tus admins usan el sistema más frecuentemente o quieres priorizar seguridad,
            // puedes invertir el orden (buscar primero en admins).
            try {
                userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    tokenValido = true;
                }
            } catch (UsernameNotFoundException e) {
                // No es cliente, continuamos...
            }

            // ------------------------------------------------------------------
            // ESTRATEGIA: INTENTO 2 - BUSCAR EN BACKOFFICE (ADMINS)
            // ------------------------------------------------------------------
            // Solo buscamos aquí si el paso anterior falló (tokenValido es false)
            if (!tokenValido) {
                try {
                    UserDetails adminDetails = this.adminUserDetailsService.loadUserByUsername(userEmail);
                    if (jwtService.isTokenValid(jwt, adminDetails)) {
                        userDetails = adminDetails;
                        tokenValido = true;
                    }
                } catch (UsernameNotFoundException e) {
                    // No existe en ninguna tabla
                }
            }

            // 3. Si encontramos usuario y el token es válido, autenticamos
            if (tokenValido && userDetails != null) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Establecer autenticación en el contexto
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}