package com.example.front_office.controller.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductoRequest {
    private String sku;
    private String ean;
    private String nombre;
    private String descripcion;
    private String urlImagen;
    private BigDecimal precio;
    private Integer stock;
    private Integer idCategoria; // <--- Clave: Solo recibimos el ID
}