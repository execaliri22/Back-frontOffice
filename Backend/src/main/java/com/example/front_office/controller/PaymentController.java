package com.example.front_office.controller;

import com.example.front_office.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/crear_preferencia")
    public ResponseEntity<?> crearPreferencia() {
        String urlPago = paymentService.crearPreferencia();

        if (urlPago == null) {
            // Si falló, devolvemos un error 500 al frontend en lugar de explotar
            return ResponseEntity.internalServerError()
                    .body(java.util.Collections.singletonMap("error", "Error al conectar con Mercado Pago"));
        }

        // Si todo salió bien
        return ResponseEntity.ok(java.util.Collections.singletonMap("url", urlPago));
    }
}