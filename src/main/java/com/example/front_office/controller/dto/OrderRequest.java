package com.example.front_office.controller.dto;

import java.math.BigDecimal;

// Puedes usar Lombok si lo tienes para simplificar getters/setters
// @Getter
// @Setter
public class OrderRequest {

    // Total de la compra, siempre manejado como BigDecimal o Double en Java
    private Double total;

    // Moneda de la transacción (ej. "USD", "EUR")
    private String currency;

    // Constructor vacío (necesario para la deserialización de JSON por Spring)
    public OrderRequest() {}

    // Constructor con campos (opcional)
    public OrderRequest(Double total, String currency) {
        this.total = total;
        this.currency = currency;
    }

    // Getters y Setters
    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}