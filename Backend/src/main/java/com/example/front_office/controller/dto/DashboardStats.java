package com.example.front_office.controller.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class DashboardStats {
    private long totalUsuarios;
    private long totalProductos;
    private long pedidosPendientes; // Estado PROCESANDO
    private BigDecimal ventasTotales; // Suma de pedidos PAGADOS/ENTREGADOS
}