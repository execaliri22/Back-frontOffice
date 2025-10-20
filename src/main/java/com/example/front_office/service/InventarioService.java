package com.example.front_office.service;
import com.example.front_office.model.Pedido;
import org.springframework.stereotype.Service;
@Service
public class InventarioService {
    public boolean reservarStock(@SuppressWarnings("rawtypes") Pedido pedido) { return true; }
    @SuppressWarnings("rawtypes")
    public void actualizarStock(Pedido pedido) { }
    @SuppressWarnings("rawtypes")
    public void liberarStock(Pedido pedido) { }
}