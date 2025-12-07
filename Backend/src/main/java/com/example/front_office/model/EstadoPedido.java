package com.example.front_office.model;

public enum EstadoPedido {
    PROCESANDO, // Estado inicial
    PAGADO,     // Nuevo: Pago exitoso
    RECHAZADO,  // Nuevo: Pago fallido
    EN_CAMINO,
    ENTREGADO,
    INCIDENCIA
}