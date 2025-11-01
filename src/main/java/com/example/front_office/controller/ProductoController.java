package com.example.front_office.controller;

import com.example.front_office.controller.dto.ProductoDTO; // Importa el DTO
import com.example.front_office.model.Categoria;
import com.example.front_office.model.Producto;
import com.example.front_office.repository.CategoriaRepository;
import com.example.front_office.service.ProductoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // --- GET (Leer Todos) ---
    @GetMapping
    @SuppressWarnings("rawtypes")
    public List<Producto> listarProductos() {
        return productoService.getAllProductos();
    }

    // --- GET (Leer por ID de Producto) ---
    @GetMapping("/{id}")
    @SuppressWarnings("rawtypes")
    public ResponseEntity<Producto> obtenerProducto(@PathVariable Integer id) {
        Optional<Producto> producto = productoService.getProductoById(id);
        return producto.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    // --- NUEVO ENDPOINT: GET (Leer por ID de Categoría) ---
    /**
     * Obtiene todos los productos pertenecientes a una categoría específica.
     * Responde a: GET /api/productos/categoria/{idCategoria} (ej. /api/productos/categoria/2)
     * @param idCategoria El ID de la categoría a filtrar (viene de la URL).
     * @return Lista de productos de esa categoría y estado 200 OK (puede ser lista vacía).
     */
    @GetMapping("/categoria/{idCategoria}")
    @SuppressWarnings("rawtypes") // Mantener si se devuelve la entidad directamente
    public ResponseEntity<List<Producto>> listarProductosPorCategoria(@PathVariable Integer idCategoria) {
        // Verifica primero si la categoría existe (opcional, pero buena práctica)
        if (!categoriaRepository.existsById(idCategoria)) {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // O devuelve una lista vacía si prefieres
             // return ResponseEntity.ok(List.of()); // Devuelve 200 OK con lista vacía
        }
        List<Producto> productos = productoService.getProductosByCategoriaId(idCategoria);
        return ResponseEntity.ok(productos); // Siempre devuelve 200 OK, incluso si la lista está vacía
    }


    // --- POST (Crear) ---
    @PostMapping
    public ResponseEntity<?> crearProducto(@RequestBody ProductoDTO productoDTO) {
        try {
            Categoria categoria = categoriaRepository.findById(productoDTO.idCategoria())
                    .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + productoDTO.idCategoria()));

            Producto nuevoProducto = new Producto();
            nuevoProducto.setSku(productoDTO.sku());
            nuevoProducto.setEan(productoDTO.ean());
            nuevoProducto.setUrlImagen(productoDTO.urlImagen());
            nuevoProducto.setNombre(productoDTO.nombre());
            nuevoProducto.setPrecio(productoDTO.precio());
            nuevoProducto.setStock(productoDTO.stock());
            nuevoProducto.setCategoria(categoria);

            @SuppressWarnings("rawtypes")
            Producto productoGuardado = productoService.saveProducto(nuevoProducto);
            return ResponseEntity.status(HttpStatus.CREATED).body(productoGuardado);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el producto: " + e.getMessage());
        }
    }

    // --- PUT (Actualizar) ---
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Integer id, @RequestBody ProductoDTO productoDTO) {
        try {
            @SuppressWarnings("rawtypes")
            Producto productoExistente = productoService.getProductoById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));

            Categoria categoria = productoExistente.getCategoria();
            if (productoDTO.idCategoria() != null && !productoDTO.idCategoria().equals(categoria.getIdCategoria())) {
                categoria = categoriaRepository.findById(productoDTO.idCategoria())
                        .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + productoDTO.idCategoria()));
            }

            productoExistente.setSku(productoDTO.sku());
            productoExistente.setEan(productoDTO.ean());
            productoExistente.setUrlImagen(productoDTO.urlImagen());
            productoExistente.setNombre(productoDTO.nombre());
            productoExistente.setPrecio(productoDTO.precio());
            productoExistente.setStock(productoDTO.stock());
            productoExistente.setCategoria(categoria);

            @SuppressWarnings("rawtypes")
            Producto productoActualizado = productoService.saveProducto(productoExistente);
            return ResponseEntity.ok(productoActualizado);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar el producto: " + e.getMessage());
       }
    }

    // --- DELETE (Eliminar) ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Integer id) {
        boolean eliminado = productoService.deleteProductoById(id);
        if (eliminado) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}