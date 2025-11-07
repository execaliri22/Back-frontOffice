package com.example.front_office.controller;

import com.example.front_office.model.Pedido;
import com.example.front_office.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

record PagoRequest(Integer pedidoId, String token) {}

@RestController
@RequestMapping("/checkout")
public class CheckoutController {
    @Autowired private PedidoService pedidoService;

    @PostMapping("/pagar")
    public ResponseEntity<String> procesarPago(@RequestBody PagoRequest request) {
        try {
            @SuppressWarnings("rawtypes")
            Pedido pedido = pedidoService.procesarPago(request.pedidoId(), request.token());
            return ResponseEntity.ok("Pago " + pedido.getEstado() + ". Redirigiendo a página de confirmación...");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error en el pago: " + e.getMessage());
        }
    }
}