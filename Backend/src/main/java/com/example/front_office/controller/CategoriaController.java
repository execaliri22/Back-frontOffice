package com.example.front_office.controller;

import com.example.front_office.model.Categoria;
import com.example.front_office.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    // --- GET (Listar todas) ---
    // Este es el endpoint público que usa tu Frontend para los filtros
    @GetMapping
    public ResponseEntity<List<Categoria>> listarTodas() {
        return ResponseEntity.ok(categoriaService.findAll());
    }

    // --- GET (Obtener una por ID) ---
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> obtenerPorId(@PathVariable Integer id) {
        return categoriaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- POST (Crear) ---
    // Requiere Rol ADMIN (configurado en SecurityConfig)
    @PostMapping
    public ResponseEntity<Categoria> crearCategoria(@RequestBody Categoria categoria) {
        // Aseguramos ID null para crear uno nuevo y no sobreescribir
        // Nota: Asegúrate que tu modelo tenga el setter 'setIdCategoria' o usa el Builder si prefieres
        categoria.setIdCategoria(null);
        return ResponseEntity.ok(categoriaService.save(categoria));
    }

    // --- PUT (Actualizar) ---
    // Requiere Rol ADMIN
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizarCategoria(@PathVariable Integer id, @RequestBody Categoria datosNuevos) {
        return categoriaService.findById(id)
                .map(catExistente -> {
                    catExistente.setNombre(datosNuevos.getNombre());
                    catExistente.setDescripcion(datosNuevos.getDescripcion()); // Agregado por si tienes descripción
                    return ResponseEntity.ok(categoriaService.save(catExistente));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // --- DELETE (Borrar) ---
    // Requiere Rol ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Integer id) {
        if (categoriaService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        categoriaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}