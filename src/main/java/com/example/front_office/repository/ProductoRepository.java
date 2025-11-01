package com.example.front_office.repository;

import com.example.front_office.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // <-- IMPORTAR LIST

@SuppressWarnings("rawtypes")
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    /**
     * Busca todos los productos que pertenecen a una categoría específica.
     * El nombre sigue la convención de Spring Data JPA para generar la consulta automáticamente.
     * Busca por el campo 'categoria' y dentro de él, por el campo 'idCategoria'.
     *
     * @param idCategoria El ID de la categoría por la cual filtrar.
     * @return Una lista de productos pertenecientes a esa categoría.
     */
    List<Producto> findByCategoriaIdCategoria(Integer idCategoria); // <-- NUEVO MÉTODO
}