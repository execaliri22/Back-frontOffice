package com.example.front_office.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

    private String token;

    // Si a futuro quieres devolver más cosas al hacer login (ej: refresh token, o mensaje),
    // solo agregas los campos aquí.
}