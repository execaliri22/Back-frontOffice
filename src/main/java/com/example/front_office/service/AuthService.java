package com.example.front_office.service;

import com.example.front_office.controller.dto.LoginRequest;
import com.example.front_office.controller.dto.RegisterRequest;
import com.example.front_office.model.Usuario; // Asegúrate de importar Usuario
import com.example.front_office.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// UserDetails ya estaba
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap; // <-- Importa HashMap
import java.util.Map;     // <-- Importa Map

@SuppressWarnings("unused")
@Service
public class AuthService {
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;
    @Autowired private AuthenticationManager authenticationManager;

    @SuppressWarnings("rawtypes")
    public String register(RegisterRequest request) {
        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());
        usuario.setContrasenaHash(passwordEncoder.encode(request.password()));
        usuario.setDireccion(request.direccion());
        // Inicialmente no hay foto de perfil
        usuario.setFotoPerfilUrl(null);
        Usuario usuarioGuardado = usuarioRepository.save(usuario); // Guarda y obtén el usuario con ID

        // *** AÑADIR NOMBRE AL TOKEN ***
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("nombre", usuarioGuardado.getNombre()); // Añade el nombre del usuario recién guardado
        // No añadimos fotoPerfilUrl aquí porque es null al registrarse

        // Pasa los extraClaims al generar el token
        return jwtService.generateToken(extraClaims, usuarioGuardado); // Usa el usuario guardado como UserDetails
    }

    @SuppressWarnings("rawtypes") // Añadido para consistencia si tu IDE lo sugiere
    public String login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        // Busca el usuario completo para obtener el nombre y la foto
        Usuario usuario = usuarioRepository.findByEmail(request.email())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado")); // Lanza excepción si no se encuentra

        // *** AÑADIR NOMBRE Y FOTO AL TOKEN ***
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("nombre", usuario.getNombre()); // Añade el nombre del usuario encontrado
        if (usuario.getFotoPerfilUrl() != null) { // Añadir URL si existe
            extraClaims.put("fotoPerfilUrl", usuario.getFotoPerfilUrl());
        }

        // Pasa los extraClaims al generar el token
        // (usuario implementa UserDetails, así que podemos pasarlo directamente)
        return jwtService.generateToken(extraClaims, usuario);
    }
}