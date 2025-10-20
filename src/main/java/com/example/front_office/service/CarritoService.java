package com.example.front_office.service;

import com.example.front_office.model.Carrito;
import com.example.front_office.model.ItemCarrito;
import com.example.front_office.model.Producto;
import com.example.front_office.model.Usuario;
import com.example.front_office.repository.CarritoRepository;
import com.example.front_office.repository.ProductoRepository;
import com.example.front_office.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

@Service
public class CarritoService {
    @Autowired private CarritoRepository carritoRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Transactional
    public Carrito agregarItem(Integer idUsuario, Integer idProducto, int cantidad) {
        Producto producto = productoRepository.findById(idProducto)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Carrito carrito = carritoRepository.findByUsuarioIdUsuario(idUsuario).orElseGet(() -> {
            Carrito nuevoCarrito = new Carrito();
            nuevoCarrito.setUsuario(usuario);
            nuevoCarrito.setFechaCreacion(new Date());
            return nuevoCarrito;
        });

        Optional<ItemCarrito> itemExistente = carrito.getItems().stream()
            .filter(item -> ((ItemCarrito) item).getProducto().getIdProducto().equals(idProducto))
            .findFirst();

        if (itemExistente.isPresent()) {
            ItemCarrito item = itemExistente.get();
            item.setCantidad(item.getCantidad() + cantidad);
            item.setSubtotal(producto.getPrecio().multiply(new BigDecimal(item.getCantidad())));
        } else {
            ItemCarrito nuevoItem = new ItemCarrito();
            nuevoItem.setCarrito(carrito);
            nuevoItem.setProducto(producto);
            nuevoItem.setCantidad(cantidad);
            nuevoItem.setSubtotal(producto.getPrecio().multiply(new BigDecimal(cantidad)));
            carrito.getItems().add(nuevoItem);
        }
        return carritoRepository.save(carrito);
    }
}