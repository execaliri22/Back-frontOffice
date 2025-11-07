package com.example.front_office.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @ManyToOne(fetch = FetchType.EAGER) // EAGER para ver siempre el producto
    @JoinColumn(name = "id_producto")
    // No necesita back-reference si Producto no tiene List<ItemCarrito>
    private Producto producto;

    @SuppressWarnings("rawtypes")
    @ManyToOne(fetch = FetchType.LAZY) // LAZY, no necesitamos el carrito completo desde el ítem
    @JoinColumn(name = "id_carrito")
    @JsonBackReference("carrito-items") // <-- Lado "inverso", no se serializa desde Carrito
    private Carrito carrito;

    private int cantidad;
    private BigDecimal subtotal;
}