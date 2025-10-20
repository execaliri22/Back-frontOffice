package com.example.front_office.repository;

import com.example.front_office.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

@SuppressWarnings("rawtypes")
public interface ProductoRepository extends JpaRepository<Producto, Integer> {}