package com.example.front_office.service;

import com.example.front_office.controller.dto.AdminLoginRequest;
import com.example.front_office.controller.dto.AdminRegisterRequest;
import com.example.front_office.controller.dto.AuthResponse;
import com.example.front_office.model.UsuarioBack;
import com.example.front_office.repository.UsuarioBackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final UsuarioBackRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // LOGIN ADMIN
    public AuthResponse login(AdminLoginRequest request) {
        // 1. Buscar usuario en tabla de admins
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Admin no encontrado"));

        // 2. Verificar password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        // 3. Generar Token con el claim de ROL explícito
        return generarTokenConRol(user);
    }

    // REGISTRO ADMIN
    public AuthResponse register(AdminRegisterRequest request) {
        var user = UsuarioBack.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                // .role(Role.ADMIN) <--- ¡ESTA LÍNEA SE ELIMINA! (Causaba el error)
                .build();

        repository.save(user);

        return generarTokenConRol(user);
    }

    // Método auxiliar para evitar repetir código y asegurar que el token siempre tenga el rol
    private AuthResponse generarTokenConRol(UsuarioBack user) {
        Map<String, Object> extraClaims = new HashMap<>();

        // Aquí forzamos que el token diga "Soy Admin"
        extraClaims.put("role", "ROLE_ADMIN");
        extraClaims.put("nombre", user.getNombre());

        var jwtToken = jwtService.generateToken(extraClaims, user);

        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }
}