package com.example.front_office.repository;

import com.example.front_office.model.UsuarioBack;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioBackRepository extends JpaRepository<UsuarioBack, Integer> {
    Optional<UsuarioBack> findByEmail(String email);
}