package com.example.front_office.service;

import com.example.front_office.controller.dto.AuthenticationResponse;
import com.example.front_office.controller.dto.LoginRequest;
import com.example.front_office.controller.dto.RegisterRequest;
import com.example.front_office.model.UsuarioBack;
import com.example.front_office.repository.UsuarioBackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder; // <--- Importante
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BackOfficeAuthService {

    private final UsuarioBackRepository usuarioBackRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder; // <--- Necesario para el registro

    // --- LOGIN ---
    public AuthenticationResponse loginAdmin(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UsuarioBack admin = usuarioBackRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Admin no encontrado"));

        return generarTokenResponse(admin);
    }

    // --- REGISTRO (Corregido) ---
    public AuthenticationResponse register(RegisterRequest request) {

        // 1. Validar duplicados
        if (usuarioBackRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("El admin ya existe");
        }

        // 2. Construir objeto (SIN EL CAMPO ROLE)
        UsuarioBack user = UsuarioBack.builder()
                .nombre(request.nombre())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        usuarioBackRepository.save(user);

        return generarTokenResponse(user);
    }

    // Método privado para reutilizar lógica
    private AuthenticationResponse generarTokenResponse(UsuarioBack user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ROLE_ADMIN"); // Aquí es donde "forzamos" el rol en el token
        extraClaims.put("nombre", user.getNombre());

        String token = jwtService.generateToken(extraClaims, user);

        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }
}