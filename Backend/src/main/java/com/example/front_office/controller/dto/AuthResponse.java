package com.example.front_office.controller.dto;

import lombok.AllArgsConstructor; // <--- AGREGAR
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor; // <--- AGREGAR

@Data
@Builder
@AllArgsConstructor // <--- Esto permite usar: new AuthResponse(token)
@NoArgsConstructor  // <--- Esto ayuda a evitar errores de JSON
public class AuthResponse {
    private String token;
}