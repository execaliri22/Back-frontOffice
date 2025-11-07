package com.example.front_office.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
public class Carrito {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCarrito;

    @Temporal(TemporalType.TIMESTAMP) // Mejor para fechas con hora
    private Date fechaCreacion;

    @SuppressWarnings("rawtypes")
    @OneToOne(fetch = FetchType.LAZY) // Considera LAZY fetching
    @JoinColumn(name = "id_usuario", referencedColumnName = "idUsuario")
    @JsonBackReference("usuario-carrito") // <-- Lado "inverso", no se serializa desde Usuario
    private Usuario usuario;


    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER) // EAGER puede ser necesario para verlo siempre
    @JsonManagedReference("carrito-items") // <-- Lado "principal", se serializa
    private List<ItemCarrito> items = new ArrayList<>();
}