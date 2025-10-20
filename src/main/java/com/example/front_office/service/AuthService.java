package com.example.front_office.service;

import com.example.front_office.controller.dto.LoginRequest;
import com.example.front_office.controller.dto.RegisterRequest;
import com.example.front_office.model.Usuario;
import com.example.front_office.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        usuarioRepository.save(usuario);
        return jwtService.generateToken(usuario);
    }

    public String login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        UserDetails user = usuarioRepository.findByEmail(request.email())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return jwtService.generateToken(user);
    }
}