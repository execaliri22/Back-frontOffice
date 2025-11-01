package com.example.front_office.controller.dto;

import java.math.BigDecimal;

public record ProductoDTO(
    String sku,
    String ean,
    String urlImagen,
    String nombre,
    BigDecimal precio,
    Integer stock,
    Integer idCategoria // Asegúrate de que este campo exista
) {}