package com.example.front_office.repository;

import com.example.front_office.model.Pedido;
import com.example.front_office.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    // Busca todos los pedidos de un usuario concreto
    List<Pedido> findByUsuario(Usuario usuario);
}