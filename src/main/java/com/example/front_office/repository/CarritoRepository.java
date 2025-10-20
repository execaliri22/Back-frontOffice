package com.example.front_office.repository;

import com.example.front_office.model.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

@SuppressWarnings("rawtypes")
public interface CarritoRepository extends JpaRepository<Carrito, Integer> {
    @SuppressWarnings("rawtypes")
    Optional<Carrito> findByUsuarioIdUsuario(Integer usuarioId);
}