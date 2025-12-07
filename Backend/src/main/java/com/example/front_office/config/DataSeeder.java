package com.example.front_office.config;

import com.example.front_office.model.UsuarioBack;
import com.example.front_office.repository.UsuarioBackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final UsuarioBackRepository usuarioBackRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initAdmin() {
        return args -> {
            // Verificar si ya existe al menos un admin
            if (usuarioBackRepository.count() == 0) {
                UsuarioBack admin = UsuarioBack.builder()
                        .nombre("Super Admin")
                        .email("admin@admin.com")
                        // La contraseña será "admin123" (cámbiala antes de producción)
                        .password(passwordEncoder.encode("admin123"))
                        .build();

                usuarioBackRepository.save(admin);
                System.out.println("✅ ADMIN CREADO POR DEFECTO: admin@admin.com / admin123");
            }
        };
    }
}