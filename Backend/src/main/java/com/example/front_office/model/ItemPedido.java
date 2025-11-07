package com.example.front_office.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @ManyToOne(fetch = FetchType.EAGER) // EAGER para ver siempre el producto
    @JoinColumn(name = "id_producto")
     // No necesita back-reference si Producto no tiene List<ItemPedido>
    private Producto producto;

    @SuppressWarnings("rawtypes")
    @ManyToOne(fetch = FetchType.LAZY) // LAZY, no necesitamos el pedido completo desde el ítem
    @JoinColumn(name = "id_pedido")
    @JsonBackReference("pedido-items") // <-- Lado "inverso", no se serializa desde Pedido
    private Pedido pedido;

    private int cantidad;
    private BigDecimal subtotal;
}