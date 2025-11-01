package com.example.front_office.service;

import com.example.front_office.model.Order;
import com.example.front_office.model.OrderItem;
import com.example.front_office.repository.OrderRepository; // <-- Nuevo Import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante para la persistencia

import java.time.LocalDateTime;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Crea y persiste una nueva orden en la base de datos.
     */
    @Transactional // Asegura que todas las operaciones de DB se ejecuten juntas
    public Order createOrder(Order newOrder) {

        // 1. Lógica de Negocio/Validación (Recalculando Total)

        double total = newOrder.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        // 2. Establecer metadatos y relaciones
        newOrder.setTotal(total);
        newOrder.setStatus("PENDING"); // El estado inicial antes del pago
        newOrder.setOrderDate(LocalDateTime.now());

        // CRUCIAL: Asegurar la relación bidireccional (Order -> OrderItem)
        if (newOrder.getItems() != null) {
            for (OrderItem item : newOrder.getItems()) {
                item.setOrder(newOrder); // Asigna la Order al OrderItem
            }
        }

        // 3. Guardar la orden y sus ítems asociados
        return orderRepository.save(newOrder);
    }
}