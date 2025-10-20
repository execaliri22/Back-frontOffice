package com.example.front_office.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
public class ItemPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @SuppressWarnings("rawtypes")
    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @SuppressWarnings("rawtypes")
    @ManyToOne
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    private int cantidad;
    private BigDecimal subtotal;
}