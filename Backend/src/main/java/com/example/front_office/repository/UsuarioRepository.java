package com.example.front_office.repository;

import com.example.front_office.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);

    // --- NUEVO ---
    Optional<Usuario> findByVerificationToken(String token);
}