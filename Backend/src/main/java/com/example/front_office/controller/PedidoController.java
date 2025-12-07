package com.example.front_office.controller;

import com.example.front_office.model.EstadoPedido;
import com.example.front_office.model.Pedido;
import com.example.front_office.model.Usuario;
import com.example.front_office.repository.PedidoRepository;
import com.example.front_office.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;

    // ---------------------------------------------------
    // ZONA ADMIN (Back Office) - Requiere Rol ADMIN
    // ---------------------------------------------------

    // 1. Ver TODOS los pedidos de la tienda
    // URL: GET /api/pedidos/admin/todos
    @GetMapping("/admin/todos")
    public ResponseEntity<List<Pedido>> obtenerTodosLosPedidos() {
        return ResponseEntity.ok(pedidoRepository.findAll());
    }

    // 2. Cambiar estado (De Procesando -> Enviado, etc.)
    // URL: PUT /api/pedidos/admin/{id}/estado?nuevoEstado=EN_CAMINO
    @PutMapping("/admin/{id}/estado")
    public ResponseEntity<Pedido> actualizarEstado(
            @PathVariable Integer id,
            @RequestParam EstadoPedido nuevoEstado
    ) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        pedido.setEstado(nuevoEstado);
        return ResponseEntity.ok(pedidoRepository.save(pedido));
    }

    // ---------------------------------------------------
    // ZONA CLIENTE (Front Office)
    // ---------------------------------------------------

    // 3. Ver MIS pedidos (Historial del usuario logueado)
    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<Pedido>> misPedidos() {
        // Obtenemos el email del token JWT
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(pedidoRepository.findByUsuario(usuario));
    }

    // Aquí faltaría el @PostMapping para CREAR el pedido (Checkout),
    // que suele ser más complejo porque involucra mover items del Carrito al Pedido.
}