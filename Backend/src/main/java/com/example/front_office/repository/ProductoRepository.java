package com.example.front_office.repository;

import com.example.front_office.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    List<Producto> findByActivoTrue();
    List<Producto> findByCategoria_IdCategoria(Integer idCategoria);
    
}