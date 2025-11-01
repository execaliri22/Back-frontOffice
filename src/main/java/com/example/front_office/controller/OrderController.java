package com.example.front_office.controller;
import com.example.front_office.model.Order;
import com.example.front_office.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:4200") // Para permitir llamadas desde Angular
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Endpoint para crear una nueva orden (POST /api/orders).
     * @param order La orden enviada en el cuerpo de la petición (JSON).
     * @return ResponseEntity con la orden creada y el estado 201 CREATED.
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        try {
            Order createdOrder = orderService.createOrder(order);
            // Devuelve el estado 201 Created y el cuerpo de la orden
            return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
        } catch (Exception e) {
            // Manejo básico de errores
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}