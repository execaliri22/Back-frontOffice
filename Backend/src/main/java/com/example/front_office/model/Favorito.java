package com.example.front_office.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor; // Constructor sin argumentos útil para JPA
import lombok.AllArgsConstructor; // Constructor con todos los argumentos

@Entity
@Data
@NoArgsConstructor // Lombok generará el constructor vacío
@AllArgsConstructor // Lombok generará constructor con todos los campos
public class Favorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Usar Long para IDs es común

    // Relación ManyToOne con Usuario (muchos favoritos pueden pertenecer a un usuario)
    // No usamos JsonBackReference aquí porque probablemente no cargaremos favoritos desde Usuario directamente
    @ManyToOne(fetch = FetchType.LAZY) // LAZY: No cargar el usuario a menos que se necesite
    @JoinColumn(name = "id_usuario", nullable = false) // Columna en la tabla Favorito
    private Usuario usuario;

    // Relación ManyToOne con Producto (muchos favoritos pueden apuntar al mismo producto)
    // No usamos JsonBackReference aquí
    @ManyToOne(fetch = FetchType.EAGER) // EAGER: Queremos ver el producto al obtener el favorito
    @JoinColumn(name = "id_producto", nullable = false) // Columna en la tabla Favorito
    private Producto producto;

    // Constructor útil si no usas Lombok @AllArgsConstructor
    // public Favorito(Usuario usuario, Producto producto) {
    //     this.usuario = usuario;
    //     this.producto = producto;
    // }
}