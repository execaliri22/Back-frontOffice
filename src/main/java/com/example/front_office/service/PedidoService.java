package com.example.front_office.service;

import com.example.front_office.model.Pedido;
import com.example.front_office.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private InventarioService inventarioService;
    @Autowired private PasarelaPagoService pasarelaPagoService;
    @Autowired private EmailService emailService;

    @SuppressWarnings("rawtypes")
    @Transactional
    public Pedido procesarPago(Integer pedidoId, String token) {
        @SuppressWarnings("rawtypes")
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        boolean stockReservado = inventarioService.reservarStock(pedido);
        if (!stockReservado) throw new RuntimeException("Stock no disponible");

        boolean pagoExitoso = pasarelaPagoService.realizarCargo(token, pedido.getTotal());

        //pasar a pasarelaPagoService la logica de pago
        if (pagoExitoso) {
            pedido.setEstado("PAGADO");
            inventarioService.actualizarStock(pedido);
            emailService.enviarConfirmacion(pedidoId);
            
        } else {
            pedido.setEstado("RECHAZADO");
            inventarioService.liberarStock(pedido);
        }
        return pedidoRepository.save(pedido);
    }
}