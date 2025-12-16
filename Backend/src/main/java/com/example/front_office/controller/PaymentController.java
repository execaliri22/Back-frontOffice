package com.example.front_office.controller;

import com.example.front_office.model.Pedido;
import com.example.front_office.repository.PedidoRepository;
import com.example.front_office.service.PaymentService;
import com.example.front_office.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Collections;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PaymentController {

    private final PedidoRepository pedidoRepository;
    private final PaymentService paymentService;
    private final PedidoService pedidoService;

    // Endpoint simple de prueba (opcional)
    @PostMapping("/crear_preferencia")
    public ResponseEntity<?> crearPreferenciaTest() {
        return ResponseEntity.ok().build();
    }

    // --- 1. ENDPOINT REAL (Recibe el ID del pedido) ---
    @PostMapping("/crear_preferencia/{idPedido}")
    public ResponseEntity<?> crearPreferencia(@PathVariable Integer idPedido) {

        // Buscamos el pedido en la BD
        Pedido pedido = pedidoRepository.findById(idPedido).orElse(null);

        if (pedido == null) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "Pedido no encontrado"));
        }

        // Pasamos el pedido real al servicio
        String url = paymentService.crearPreferencia(pedido);

        if (url == null) {
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("error", "Error al crear preferencia en MP"));
        }

        return ResponseEntity.ok(Collections.singletonMap("url", url));
    }

    // --- 2. ENDPOINT DE RETORNO (Success) ---
    @GetMapping("/success")
    public RedirectView pagoExitoso(
            @RequestParam("collection_status") String status,
            @RequestParam("external_reference") String referenciaPedido) {

        // Verificamos que el pago esté aprobado
        if ("approved".equalsIgnoreCase(status)) {
            try {
                // Recuperamos el ID del pedido
                Integer pedidoId = Integer.parseInt(referenciaPedido);

                // Actualizamos el estado del pedido a PROCESANDO
                pedidoService.confirmarPedidoExitoso(pedidoId);

                System.out.println("--- ¡Pago confirmado para Pedido #" + pedidoId + "! ---");

            } catch (NumberFormatException e) {
                System.err.println("Error: ID de pedido inválido: " + referenciaPedido);
            } catch (Exception e) {
                System.err.println("Error actualizando pedido: " + e.getMessage());
            }
        }

        return null;
    }
}