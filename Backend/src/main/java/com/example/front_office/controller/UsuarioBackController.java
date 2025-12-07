package com.example.front_office.controller;

import com.example.front_office.model.UsuarioBack;
import com.example.front_office.repository.UsuarioBackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backoffice/usuarios") // Ruta base para gestión
@RequiredArgsConstructor
public class UsuarioBackController {

    private final UsuarioBackRepository repository;

    // 1. Listar todos los administradores
    @GetMapping
    public ResponseEntity<List<UsuarioBack>> listarAdmins() {
        return ResponseEntity.ok(repository.findAll());
    }

    // 2. Obtener un admin por ID
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioBack> obtenerAdmin(@PathVariable Integer id) {
        return repository.findById(id) // Ahora sí coinciden los tipos
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Eliminar un admin (Cuidado: no te elimines a ti mismo en el front)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAdmin(@PathVariable Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // El "Crear" ya lo tienes en /auth/register,
    // pero si quieres uno interno sin token de retorno, podrías agregarlo aquí.
}