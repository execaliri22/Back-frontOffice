package com.example.front_office.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
public class ItemCarrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @SuppressWarnings("rawtypes")
    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @SuppressWarnings("rawtypes")
    @ManyToOne
    @JoinColumn(name = "id_carrito")
    private Carrito carrito;

    private int cantidad;
    private BigDecimal subtotal;
}