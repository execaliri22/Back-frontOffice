package com.example.front_office.controller;

import com.example.front_office.model.Categoria;
import com.example.front_office.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backoffice/categorias") // <--- Coincide con tu SecurityConfig
@RequiredArgsConstructor
public class AdminCategoriaController {

    private final CategoriaRepository categoriaRepository;

    @GetMapping
    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Categoria> crear(@RequestBody Categoria categoria) {
        return ResponseEntity.ok(categoriaRepository.save(categoria));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> editar(@PathVariable Integer id, @RequestBody Categoria datos) {
        Categoria cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        cat.setNombre(datos.getNombre());
        cat.setDescripcion(datos.getDescripcion());

        return ResponseEntity.ok(categoriaRepository.save(cat));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        categoriaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}