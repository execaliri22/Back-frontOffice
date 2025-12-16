package com.example.front_office.controller;

import com.example.front_office.controller.dto.ProductoDTO;
import com.example.front_office.model.Categoria;
import com.example.front_office.model.Producto;
import com.example.front_office.repository.CategoriaRepository;
import com.example.front_office.repository.ProductoRepository;
import com.example.front_office.service.ProductoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor // Inyección de dependencias moderna
public class ProductoController {

    private final ProductoService productoService;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    // --- GET (Leer Todos) ---
    @GetMapping
    public ResponseEntity<List<Producto>> listarProductos() {
        return ResponseEntity.ok(productoService.getAllProductos());
    }

    // --- GET (Leer por ID de Producto) ---
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerProducto(@PathVariable Integer id) {
        return productoService.getProductoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/activos")
    public List<Producto> listarActivos() {
        // Usa el filtro para la tienda
        return productoRepository.findByActivoTrue();
    }
    // --- GET (Filtrar por Categoría) ---
    @GetMapping("/categoria/{idCategoria}")
    public ResponseEntity<List<Producto>> listarProductosPorCategoria(@PathVariable Integer idCategoria) {
        // 1. Validar si la categoría existe
        if (!categoriaRepository.existsById(idCategoria)) {
            return ResponseEntity.notFound().build();
        }

        // 2. Llamar al servicio
        // Asegúrate de tener este método creado en ProductoService (ver abajo)
        List<Producto> productos = productoService.getProductosByCategoriaId(idCategoria);

        return ResponseEntity.ok(productos);
    }

    // --- POST (Crear) ---
    @PostMapping
    public ResponseEntity<?> crearProducto(@RequestBody ProductoDTO productoDTO) {
        try {
            Categoria categoria = categoriaRepository.findById(productoDTO.idCategoria())
                    .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + productoDTO.idCategoria()));

            Producto nuevoProducto = Producto.builder() // Usando Builder si lo tienes, sino new Producto()
                    .sku(productoDTO.sku())
                    .ean(productoDTO.ean())
                    .urlImagen(productoDTO.urlImagen())
                    .nombre(productoDTO.nombre())
                    .descripcion(productoDTO.descripcion()) // Asegúrate de mapear esto si está en el DTO
                    .precio(productoDTO.precio())
                    .stock(productoDTO.stock())
                    .categoria(categoria)
                    .activo(true)
                    .build();

            Producto productoGuardado = productoService.saveProducto(nuevoProducto);
            return ResponseEntity.status(HttpStatus.CREATED).body(productoGuardado);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Integer id, @RequestBody ProductoDTO productoDTO) {
        try {
            Producto productoExistente = productoService.getProductoById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

            // Actualizar categoría si cambió
            if (productoDTO.idCategoria() != null && !productoDTO.idCategoria().equals(productoExistente.getCategoria().getIdCategoria())) {
                Categoria nuevaCategoria = categoriaRepository.findById(productoDTO.idCategoria())
                        .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
                productoExistente.setCategoria(nuevaCategoria);
            }

            // Actualizar campos
            productoExistente.setSku(productoDTO.sku());
            productoExistente.setEan(productoDTO.ean());
            productoExistente.setNombre(productoDTO.nombre());
            productoExistente.setUrlImagen(productoDTO.urlImagen());
            productoExistente.setPrecio(productoDTO.precio());
            productoExistente.setStock(productoDTO.stock());
            // productoExistente.setDescripcion(productoDTO.descripcion());

            Producto actualizado = productoService.saveProducto(productoExistente);
            return ResponseEntity.ok(actualizado);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    // --- DELETE (Eliminar) ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Integer id) {
        if (productoService.deleteProductoById(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}