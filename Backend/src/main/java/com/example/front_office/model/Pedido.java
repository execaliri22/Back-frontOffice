package com.example.front_office.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @Temporal(TemporalType.TIMESTAMP) // Mejor para fechas con hora
    private Date fecha;
    private BigDecimal total;
    @Enumerated(EnumType.STRING)
    private EstadoPedido estado;
    
    @SuppressWarnings("rawtypes")
    @ManyToOne(fetch = FetchType.LAZY) // Considera LAZY fetching
    @JoinColumn(name = "id_usuario")
    @JsonBackReference("usuario-pedidos") // <-- Lado "inverso", no se serializa desde Usuario
    private Usuario usuario;


    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.EAGER) // EAGER puede ser necesario
    @JsonManagedReference("pedido-items") // <-- Lado "principal", se serializa
    private List<ItemPedido> items;
}