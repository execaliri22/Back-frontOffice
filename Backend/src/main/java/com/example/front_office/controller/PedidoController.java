package com.example.front_office.controller;

import com.example.front_office.model.*;
import com.example.front_office.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    // --- REPOSITORIOS ---
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;

    // ---------------------------------------------------
    // ZONA ADMIN
    // ---------------------------------------------------

    @GetMapping("/admin/todos")
    public ResponseEntity<List<Pedido>> obtenerTodosLosPedidos() {
        return ResponseEntity.ok(pedidoRepository.findAll());
    }

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
    // ZONA CLIENTE
    // ---------------------------------------------------

    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<Pedido>> misPedidos() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Nota: Asegúrate que tu PedidoRepository tenga el método 'findByUsuario'
        return ResponseEntity.ok(pedidoRepository.findByUsuario(usuario));
    }

    // ---------------------------------------------------
    // CHECKOUT (Crear Pedido)
    // ---------------------------------------------------
    @PostMapping
    @Transactional // Vital para asegurar consistencia de datos
    public ResponseEntity<?> crearPedido() {

        // 1. Identificar al usuario logueado
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Buscar su carrito (Usando el nombre específico de tu repo)
        Carrito carrito = carritoRepository.findByUsuarioIdUsuario(usuario.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("El usuario no tiene carrito"));

        if (carrito.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body("El carrito está vacío");
        }

        // 3. Crear cabecera del Pedido
        Pedido nuevoPedido = new Pedido();
        nuevoPedido.setUsuario(usuario);
        nuevoPedido.setFecha(new Date());
        nuevoPedido.setEstado(EstadoPedido.PROCESANDO);
        nuevoPedido.setItems(new ArrayList<>()); // Lista vacía lista para llenar

        // variable LOCAL para sumar (esto soluciona el error anterior)
        double total = 0.0;

        // 4. Procesar Items
        for (ItemCarrito itemC : carrito.getItems()) {
            Producto prod = itemC.getProducto();

            // A. Verificar Stock
            if (prod.getStock() < itemC.getCantidad()) {
                return ResponseEntity.badRequest()
                        .body("Stock insuficiente para: " + prod.getNombre());
            }

            // B. Descontar Stock
            prod.setStock(prod.getStock() - itemC.getCantidad());
            productoRepository.save(prod);

            // C. Crear Item de Pedido (Copia del ItemCarrito)
            ItemPedido itemP = new ItemPedido();
            itemP.setProducto(prod);
            itemP.setCantidad(itemC.getCantidad());
            itemP.setSubtotal(itemC.getSubtotal());
            itemP.setPedido(nuevoPedido); // Relación bidireccional

            // D. Agregar a la lista y sumar
            nuevoPedido.getItems().add(itemP);
            total += itemC.getSubtotal().doubleValue();
        }

        nuevoPedido.setTotal(BigDecimal.valueOf(total));

        // 5. Guardar Pedido (Cascade guarda los items automáticamente)
        Pedido pedidoGuardado = pedidoRepository.save(nuevoPedido);

        // 6. Limpiar Carrito
        // Importante: Asegúrate de tener orphanRemoval=true en la entidad Carrito->Items
        carrito.getItems().clear();
        carritoRepository.save(carrito);

        return ResponseEntity.ok(pedidoGuardado);
    }
}