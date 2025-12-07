package com.example.front_office.controller;

import com.example.front_office.controller.dto.ProductoRequest;
import com.example.front_office.model.Categoria;
import com.example.front_office.model.Producto;
import com.example.front_office.repository.CategoriaRepository;
import com.example.front_office.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backoffice/productos")
@RequiredArgsConstructor
public class AdminProductoController {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @GetMapping
    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody ProductoRequest request) {
        // 1. Buscamos la categoría por ID
        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no existe"));

        // 2. Creamos el producto
        Producto producto = Producto.builder()
                .sku(request.getSku())
                .ean(request.getEan())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .urlImagen(request.getUrlImagen())
                .precio(request.getPrecio())
                .stock(request.getStock())
                .activo(true) // Por defecto activo
                .categoria(categoria) // Asignamos la relación
                .build();

        return ResponseEntity.ok(productoRepository.save(producto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> editar(@PathVariable Integer id, @RequestBody ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Actualizar datos básicos
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setUrlImagen(request.getUrlImagen());
        producto.setSku(request.getSku());

        // Si cambiaron la categoría
        if (request.getIdCategoria() != null) {
            Categoria nuevaCat = categoriaRepository.findById(request.getIdCategoria())
                    .orElseThrow(() -> new RuntimeException("Categoría no existe"));
            producto.setCategoria(nuevaCat);
        }

        return ResponseEntity.ok(productoRepository.save(producto));
    }

    // Soft Delete (Desactivar en vez de borrar)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        producto.setActivo(false);
        productoRepository.save(producto);

        return ResponseEntity.noContent().build();
    }
}