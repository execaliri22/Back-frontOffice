package com.example.front_office.service;

import com.example.front_office.controller.dto.LoginRequest;
import com.example.front_office.controller.dto.RegisterRequest;
import com.example.front_office.controller.dto.AuthResponse; // Asegúrate de usar AuthResponse
import com.example.front_office.model.Usuario;
import com.example.front_office.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private EmailService emailService; // <--- NUEVO

    // CAMBIO: Ahora devuelve String (Mensaje), NO Token
    public String register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) { // Asumiendo que usas Record en RegisterRequest
            throw new RuntimeException("El correo ya está registrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());
        usuario.setContrasenaHash(passwordEncoder.encode(request.password()));
        usuario.setDireccion(request.direccion());
        usuario.setFotoPerfilUrl(null);

        // --- LÓGICA DE VERIFICACIÓN ---
        usuario.setEnabled(false); // Nace desactivado
        String token = UUID.randomUUID().toString();
        usuario.setVerificationToken(token);

        usuarioRepository.save(usuario);

        // Enviar correo
        emailService.enviarVerificacion(usuario.getEmail(), usuario.getNombre(), token);

        return "Registro exitoso. Revisa tu correo para activar la cuenta.";
    }

    public AuthResponse login(LoginRequest request) { // Usamos AuthResponse para ser prolijos
        // Esto fallará si enabled = false
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("nombre", usuario.getNombre());
        if (usuario.getFotoPerfilUrl() != null) {
            extraClaims.put("fotoPerfilUrl", usuario.getFotoPerfilUrl());
        }

        String token = jwtService.generateToken(extraClaims, usuario);

        return AuthResponse.builder().token(token).build();
    }

    // --- NUEVO MÉTODO ---
    public String verifyAccount(String token) {
        Usuario usuario = usuarioRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if(usuario.isEnabled()){
            return "La cuenta ya estaba activada.";
        }

        usuario.setEnabled(true);
        usuario.setVerificationToken(null); // Borramos el token usado
        usuarioRepository.save(usuario);

        return "Cuenta verificada con éxito. Ya puedes iniciar sesión.";
    }
}