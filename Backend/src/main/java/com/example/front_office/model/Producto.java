package com.example.front_office.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@Builder             // Permite: Producto.builder().nombre("Coca Cola").build()
@NoArgsConstructor   // Obligatorio para JPA
@AllArgsConstructor  // Obligatorio para @Builder
@Table(name = "productos")

public class Producto {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Integer idProducto;

 @Column(unique = true, nullable = false)
 private String sku; // Código interno único

 private String ean; // Código de barras (opcional)

 private String urlImagen;

 @Column(nullable = false)
 private String nombre;

 @Column(length = 1000) // Agregado: Las descripciones suelen ser largas
 private String descripcion;

 @Column(nullable = false)
 private BigDecimal precio; // BigDecimal es perfecto para dinero (evita errores de decimales)

 private Integer stock;

 // Campo nuevo recomendado: Para "borrar" lógica sin perder historial
 @Builder.Default
 private Boolean activo = true;

 // --- RELACIÓN CON CATEGORÍA ---
 // Cambiamos a LAZY por rendimiento.
 // Usamos @JsonBackReference para que al pedir un producto, NO intente serializar
 // toda la categoría y sus 500 productos de nuevo (evita bucle infinito).
 @ManyToOne(fetch = FetchType.EAGER)
 @JoinColumn(name = "id_categoria")
 @JsonIgnoreProperties({"productos", "hibernateLazyInitializer", "handler"})
 private Categoria categoria;
}