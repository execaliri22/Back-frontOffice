package com.example.front_office.controller;
import com.example.front_office.model.EstadoPedido;
import com.example.front_office.model.Pedido;
import com.example.front_office.repository.PedidoRepository;
import com.example.front_office.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/admin/pedidos") // <--- ESTO DEBE COINCIDIR CON ANGULAR
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminOrderController {

    private final PedidoRepository pedidoRepository;

    // 1. Listar todos los pedidos (para la tabla)
    @GetMapping
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll(); // O usar una consulta con orden por fecha desc
    }

    // 2. CAMBIAR ESTADO (El endpoint que está fallando)
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Integer id,
            @RequestParam String nuevoEstado) {

        System.out.println("INTENTO DE CAMBIO DE ESTADO: ID=" + id + ", ESTADO=" + nuevoEstado);

        return pedidoRepository.findById(id).map(pedido -> {
            try {
                // Convertimos el String (ej: "ENVIADO") al Enum
                pedido.setEstado(EstadoPedido.valueOf(nuevoEstado));
                pedidoRepository.save(pedido);
                return ResponseEntity.ok(Map.of("mensaje", "Estado actualizado"));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Estado inválido: " + nuevoEstado));
            }
        }).orElse(ResponseEntity.notFound().build());
    }
}