package com.example.front_office.controller;

import com.example.front_office.model.Producto;
import com.example.front_office.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/productos") // Ruta base para todos los endpoints de productos
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    /**
     * Endpoint GET para obtener todos los productos.
     * URL: GET http://localhost:8080/api/productos
     */
    @GetMapping
    @SuppressWarnings("rawtypes")
    public List<Producto> listarProductos() {
        return productoService.getAllProductos();
    }

    /**
     * Endpoint GET para obtener un producto por su ID.
     * URL: GET http://localhost:8080/api/productos/1 (por ejemplo)
     */
    @GetMapping("/{id}")
    @SuppressWarnings("rawtypes")
    public ResponseEntity<Producto> obtenerProducto(@PathVariable Integer id) {
        Optional<Producto> producto = productoService.getProductoById(id);
        
        // Si el producto existe, devuelve 200 OK con el producto
        // Si no, devuelve 404 Not Found
        return producto.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }
}