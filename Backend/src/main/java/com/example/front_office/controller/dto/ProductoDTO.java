package com.example.front_office.controller.dto;

import java.math.BigDecimal;

public record ProductoDTO(
        String sku,
        String ean,
        String nombre,
        String descripcion, 
        String urlImagen,
        BigDecimal precio,
        Integer stock,
        Integer idCategoria
) {}