package com.example.front_office.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
public class Producto {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
private Integer idProducto;

 @Column(unique = true, nullable = false)
 private String sku;

    private String ean;

 private String urlImagen;

 private String nombre;
 private BigDecimal precio;
 private Integer stock;

 // FetchType.EAGER puede ser útil si siempre quieres ver la categoría con el producto
 @ManyToOne(fetch = FetchType.EAGER)
 @JoinColumn(name = "id_categoria")
 private Categoria categoria;
}