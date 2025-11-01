package com.example.front_office.model;

import jakarta.persistence.*;
import lombok.Data;
// Podrías añadir @JsonIgnoreProperties("productos") si tuvieras List<Producto> aquí
// import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@Entity
@Data
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCategoria;
    private String nombre;

    // Si en el futuro añades @OneToMany List<Producto> productos aquí,
    // necesitarías @JsonIgnoreProperties("categoria") en esa lista
    // o usar @JsonManagedReference aquí y @JsonBackReference en Producto.categoria
}