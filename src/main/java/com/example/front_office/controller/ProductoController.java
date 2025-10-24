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
// @SuppressWarnings({"rawtypes", "unchecked"}) // Ya no son tan necesarias con el DTO correcto
public class ProductoController { // <-- Eliminado el <ProductoDTO> genérico incorrecto

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // --- GET (Leer) ---

    @GetMapping
    @SuppressWarnings("rawtypes") // Mantener si se devuelve la entidad directamente
    public List<Producto> listarProductos() {
        return productoService.getAllProductos();
    }

    @GetMapping("/{id}")
    @SuppressWarnings("rawtypes") // Mantener si se devuelve la entidad directamente
    public ResponseEntity<Producto> obtenerProducto(@PathVariable Integer id) {
        Optional<Producto> producto = productoService.getProductoById(id);
        return producto.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    // --- POST (Crear) ---

    /**
     * Endpoint POST para crear un nuevo producto.
     * URL: POST http://localhost:8080/api/productos
     * Body: { "sku": "...", "ean": "...", ..., "idCategoria": 1 }
     */
    @PostMapping
    public ResponseEntity<?> crearProducto(@RequestBody ProductoDTO productoDTO) { // <-- Usa ProductoDTO correctamente
        try {
            // Busca la categoría por el ID proporcionado en el DTO
            Categoria categoria = categoriaRepository.findById(productoDTO.idCategoria()) // <-- Acceso directo al campo del record
                    .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + productoDTO.idCategoria()));

            // Crea la entidad Producto a partir del DTO
            Producto nuevoProducto = new Producto();
            // Accede a los campos del DTO usando los métodos generados por 'record'
            nuevoProducto.setSku(productoDTO.sku());
            nuevoProducto.setEan(productoDTO.ean());
            nuevoProducto.setUrlImagen(productoDTO.urlImagen());
            nuevoProducto.setNombre(productoDTO.nombre()); // <-- Acceso directo
            nuevoProducto.setPrecio(productoDTO.precio());
            nuevoProducto.setStock(productoDTO.stock());
            nuevoProducto.setCategoria(categoria); // Asigna la categoría encontrada

            @SuppressWarnings("rawtypes") // Mantener si se devuelve la entidad directamente
            Producto productoGuardado = productoService.saveProducto(nuevoProducto);
            // Devuelve 201 Created con el producto creado
            return ResponseEntity.status(HttpStatus.CREATED).body(productoGuardado);

        } catch (EntityNotFoundException e) {
            // Si la categoría no se encuentra, devuelve 400 Bad Request
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
             // Otro error inesperado
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el producto: " + e.getMessage());
        }
    }

    // --- PUT (Actualizar) ---

    /**
     * Endpoint PUT para actualizar un producto existente.
     * URL: PUT http://localhost:8080/api/productos/1 (por ejemplo)
     * Body: { "sku": "...", "ean": "...", ..., "idCategoria": 2 }
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Integer id, @RequestBody ProductoDTO productoDTO) { // <-- Usa ProductoDTO correctamente
        try {
            // Busca el producto existente
            @SuppressWarnings("rawtypes") // Mantener si se usa la entidad directamente
            Producto productoExistente = productoService.getProductoById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));

            // Busca la nueva categoría (si se proporcionó y es diferente)
            Categoria categoria = productoExistente.getCategoria(); // Usa la existente por defecto
            // Accede a los campos del DTO usando los métodos generados por 'record'
            if (productoDTO.idCategoria() != null && !productoDTO.idCategoria().equals(categoria.getIdCategoria())) {
                categoria = categoriaRepository.findById(productoDTO.idCategoria())
                        .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + productoDTO.idCategoria()));
            }

            // Actualiza los campos del producto existente con los datos del DTO
            productoExistente.setSku(productoDTO.sku());
            productoExistente.setEan(productoDTO.ean());
            productoExistente.setUrlImagen(productoDTO.urlImagen());
            productoExistente.setNombre(productoDTO.nombre()); // <-- Acceso directo
            productoExistente.setPrecio(productoDTO.precio());
            productoExistente.setStock(productoDTO.stock());
            productoExistente.setCategoria(categoria);

            @SuppressWarnings("rawtypes") // Mantener si se devuelve la entidad directamente
            Producto productoActualizado = productoService.saveProducto(productoExistente);
            // Devuelve 200 OK con el producto actualizado
            return ResponseEntity.ok(productoActualizado);

        } catch (EntityNotFoundException e) {
            // Si el producto o la categoría no se encuentran
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Otro error inesperado
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar el producto: " + e.getMessage());
       }
    }

    // --- DELETE (Eliminar) ---

    /**
     * Endpoint DELETE para eliminar un producto.
     * URL: DELETE http://localhost:8080/api/productos/1 (por ejemplo)
     */
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