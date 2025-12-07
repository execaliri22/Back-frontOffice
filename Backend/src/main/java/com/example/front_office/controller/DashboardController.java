package com.example.front_office.controller;

import com.example.front_office.controller.dto.DashboardStats;
import com.example.front_office.model.EstadoPedido;
import com.example.front_office.repository.PedidoRepository;
import com.example.front_office.repository.ProductoRepository;
import com.example.front_office.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/backoffice/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;

    @GetMapping("/resumen")
    public ResponseEntity<DashboardStats> obtenerResumen() {
        // Lógica simple para obtener contadores
        long usuarios = usuarioRepository.count();
        long productos = productoRepository.count();

        // Contar pedidos que NO han sido finalizados (ej: solo PROCESANDO)
        // Nota: Podrías necesitar un método countByEstado en tu repo, o filtrar aquí (menos eficiente pero funciona para empezar)
        long pendientes = pedidoRepository.findAll().stream()
                .filter(p -> p.getEstado() == EstadoPedido.PROCESANDO)
                .count();

        // Sumar ventas de pedidos exitosos (PAGADO o ENTREGADO)
        BigDecimal totalVentas = pedidoRepository.findAll().stream()
                .filter(p -> p.getEstado() == EstadoPedido.PAGADO || p.getEstado() == EstadoPedido.ENTREGADO)
                .map(p -> p.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ResponseEntity.ok(DashboardStats.builder()
                .totalUsuarios(usuarios)
                .totalProductos(productos)
                .pedidosPendientes(pendientes)
                .ventasTotales(totalVentas)
                .build());
    }
}