package com.example.front_office.service;

import com.example.front_office.model.EstadoPedido; // Importante importar el Enum
import com.example.front_office.model.Pedido;
import com.example.front_office.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // Inyecta automáticamente los 'final'
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final InventarioService inventarioService;
    private final PasarelaPagoService pasarelaPagoService;
    private final EmailService emailService;

    @Transactional
    public Pedido procesarPago(Integer pedidoId, String token) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        boolean stockReservado = inventarioService.reservarStock(pedido);

        if (!stockReservado) {
            throw new RuntimeException("Stock no disponible");
        }

        boolean pagoExitoso = pasarelaPagoService.realizarCargo(token, pedido.getTotal());

        if (pagoExitoso) {
            // CORRECCIÓN: Usar el Enum, no String
            pedido.setEstado(EstadoPedido.PAGADO);
            inventarioService.actualizarStock(pedido);

            // Intenta enviar email, pero que no rompa el pedido si falla el correo
            try {
                emailService.enviarConfirmacion(pedidoId);
            } catch (Exception e) {
                // Loguear error pero no detener el flujo
                System.err.println("Error enviando email: " + e.getMessage());
            }

        } else {
            // CORRECCIÓN: Usar el Enum, no String
            pedido.setEstado(EstadoPedido.RECHAZADO);
            inventarioService.liberarStock(pedido);
        }

        return pedidoRepository.save(pedido);
    }
    @Transactional
    public void confirmarPedidoExitoso(Integer pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + pedidoId));

        // Actualizamos al estado que pediste
        pedido.setEstado(EstadoPedido.PROCESANDO);

        // Opcional: Si no descontaste stock al crear el pedido, hazlo aquí
        // inventarioService.actualizarStock(pedido);

        pedidoRepository.save(pedido);
    }
}