package com.example.front_office.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Data
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPedido;
    
    private Date fecha;
    private BigDecimal total;
    private String estado;

    @SuppressWarnings("rawtypes")
    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<ItemPedido> items; // Debe ser List<ItemPedido>
}