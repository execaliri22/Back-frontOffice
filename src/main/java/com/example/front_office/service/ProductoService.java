package com.example.front_office.service;

import com.example.front_office.model.Producto;
import com.example.front_office.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    /**
     * Obtiene todos los productos de la base de datos.
     * @return Una lista de todos los productos.
     */
    @SuppressWarnings("rawtypes")
    public List<Producto> getAllProductos() {
        return productoRepository.findAll();
    }

    /**
     * Busca un producto por su ID.
     * @param id El ID del producto.
     * @return Un Optional que contiene el producto si se encuentra, o vacío si no.
     */
    @SuppressWarnings("rawtypes")
    public Optional<Producto> getProductoById(Integer id) {
        return productoRepository.findById(id);
    }
}