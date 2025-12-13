package com.example.front_office.controller;

import com.example.front_office.model.*;
import com.example.front_office.repository.*;
import com.example.front_office.service.PaymentService; // <--- IMPORTANTE
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map; // <--- Para recibir el cuerpo del JSON

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // <--- VITAL PARA QUE NGROK/FRONTEND NO FALLE POR CORS
public class PedidoController {

    // --- REPOSITORIOS ---
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;

    // --- SERVICIOS ---
    private final PaymentService paymentService; // <--- INYECTADO PARA MERCADO PAGO

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
    // ZONA CLIENTE (Mis Pedidos)
    // ---------------------------------------------------

    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<Pedido>> misPedidos() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(pedidoRepository.findByUsuario(usuario));
    }

    // ---------------------------------------------------
    // ZONA PAGO (NUEVA LÓGICA MERCADO PAGO)
    // ---------------------------------------------------

    // 1. Iniciar el pago de un pedido existente (Genera Link)
    @PostMapping("/{id}/pagar")
    public ResponseEntity<?> iniciarPago(@PathVariable Integer id) {
        // Buscar el pedido
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // (Opcional) Validar que el pedido pertenezca al usuario logueado aquí

        // Generar la preferencia con los datos reales del pedido
        String urlPago = paymentService.crearPreferencia(pedido);

        return ResponseEntity.ok(Map.of("url", urlPago));
    }

    // 2. Confirmar que el pago fue exitoso (Callback del Frontend)
    @PutMapping("/{id}/confirmar-pago")
    @Transactional
    public ResponseEntity<?> confirmarPago(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        String paymentId = body.get("paymentId"); // ID de transacción de MP

        // Cambiar estado a PAGADO
        // ASEGÚRATE DE QUE 'PAGADO' EXISTA EN TU ENUM 'EstadoPedido'
        pedido.setEstado(EstadoPedido.PAGADO);

        // Si quisieras guardar el ID de transacción para reclamos:
        // pedido.setIdTransaccion(paymentId); // (Necesitarías crear este campo en la entidad Pedido)

        pedidoRepository.save(pedido);

        return ResponseEntity.ok(Map.of("mensaje", "Pedido pagado y confirmado exitosamente"));
    }

    // ---------------------------------------------------
    // CHECKOUT (Crear Pedido desde Carrito)
    // ---------------------------------------------------
    @PostMapping
    @Transactional
    public ResponseEntity<?> crearPedido() {

        // 1. Identificar al usuario logueado
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Buscar su carrito
        Carrito carrito = carritoRepository.findByUsuarioIdUsuario(usuario.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("El usuario no tiene carrito"));

        if (carrito.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body("El carrito está vacío");
        }

        // 3. Crear cabecera del Pedido
        Pedido nuevoPedido = new Pedido();
        nuevoPedido.setUsuario(usuario);
        nuevoPedido.setFecha(new Date());
        nuevoPedido.setEstado(EstadoPedido.PROCESANDO); // Nace como PROCESANDO
        nuevoPedido.setItems(new ArrayList<>());

        double total = 0.0;

        // 4. Procesar Items (Mover de Carrito a Pedido y descontar Stock)
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

            // C. Crear Item de Pedido
            ItemPedido itemP = new ItemPedido();
            itemP.setProducto(prod);
            itemP.setCantidad(itemC.getCantidad());
            itemP.setSubtotal(itemC.getSubtotal());
            itemP.setPedido(nuevoPedido);

            // D. Agregar y sumar
            nuevoPedido.getItems().add(itemP);
            total += itemC.getSubtotal().doubleValue();
        }

        nuevoPedido.setTotal(BigDecimal.valueOf(total));

        // 5. Guardar Pedido
        Pedido pedidoGuardado = pedidoRepository.save(nuevoPedido);

        // 6. Limpiar Carrito
        carrito.getItems().clear();
        carritoRepository.save(carrito);

        return ResponseEntity.ok(pedidoGuardado);
    }

}