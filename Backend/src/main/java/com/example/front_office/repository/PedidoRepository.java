package com.example.front_office.repository;

import com.example.front_office.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

@SuppressWarnings("rawtypes")
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {}