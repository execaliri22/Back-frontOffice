package com.example.front_office.repository;

import com.example.front_office.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // CAMBIA ESTO:
    // List<Producto> findByCategoriaId(Integer idCategoria);

    // POR ESTO (Nota el guion bajo o el nombre exacto del campo):
    List<Producto> findByCategoria_IdCategoria(Integer idCategoria);
}