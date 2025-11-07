package com.example.front_office.service;

import com.example.front_office.model.Producto; // Asegúrate que la ruta sea correcta
import com.example.front_office.repository.ProductoRepository; // Asegúrate que la ruta sea correcta
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importa Transactional

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

    /**
     * Guarda (crea o actualiza) un producto en la base de datos.
     * Si el producto tiene un ID nulo, se creará uno nuevo.
     * Si el producto tiene un ID existente, se actualizará.
     * @param producto El objeto Producto a guardar.
     * @return El producto guardado (con ID si es nuevo).
     */
    @SuppressWarnings("rawtypes")
    @Transactional // Recomendado para operaciones de escritura
    public Producto saveProducto(Producto producto) {
        // Usa el método save del repositorio, que maneja tanto creación como actualización
        return productoRepository.save(producto);
    }

    /**
     * Elimina un producto de la base de datos por su ID.
     * @param id El ID del producto a eliminar.
     * @return true si el producto existía y fue eliminado, false si no existía.
     */
    @Transactional // Recomendado para operaciones de escritura
    public boolean deleteProductoById(Integer id) {
        if (productoRepository.existsById(id)) { // Verifica si existe antes de borrar
            productoRepository.deleteById(id); // Elimina por ID
            return true; // Indica éxito
        }
        return false; // Indica que no se encontró para eliminar
    }

    /**
     * Verifica si un producto existe por su ID.
     * Útil en el controlador antes de intentar actualizar o borrar.
     * @param id El ID del producto.
     * @return true si el producto existe, false si no.
     */
    public boolean existsById(Integer id) {
        // Usa el método existsById del repositorio
        return productoRepository.existsById(id);
    }

    // --- NUEVO MÉTODO ---
    /**
     * Obtiene todos los productos que pertenecen a una categoría específica.
     * @param idCategoria El ID de la categoría.
     * @return Una lista de productos de esa categoría.
     */
    @SuppressWarnings("rawtypes")
    public List<Producto> getProductosByCategoriaId(Integer idCategoria) {
        // Llama al nuevo método definido en el repositorio
        return productoRepository.findByCategoriaIdCategoria(idCategoria);
    }
}