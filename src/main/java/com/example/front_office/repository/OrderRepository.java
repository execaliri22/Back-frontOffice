package com.example.front_office.repository;

import com.example.front_office.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Heredamos de JpaRepository, indicando la Entidad (Order) y el tipo de su ID (Long)
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Puedes añadir métodos personalizados aquí, si los necesitas.
    // Ej: List<Order> findByCustomerId(Long customerId);
}