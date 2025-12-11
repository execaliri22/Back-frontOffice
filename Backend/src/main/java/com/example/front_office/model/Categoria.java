package com.example.front_office.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder             // Permite: Categoria.builder().nombre("Ropa").build()
@NoArgsConstructor   // Obligatorio para JPA
@AllArgsConstructor  // Obligatorio para @Builder
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCategoria;

    @Column(nullable = false, unique = true) // El nombre no puede repetirse
    private String nombre;

    private String descripcion; // Campo útil para el frontend

    // --- RELACIÓN CON PRODUCTOS ---
    // mappedBy = "categoria" se refiere al nombre del atributo en la clase Producto
    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Producto> productos = new ArrayList<>();
}