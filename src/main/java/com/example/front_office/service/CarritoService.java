package com.example.front_office.service;

import com.example.front_office.model.Carrito;
import com.example.front_office.model.ItemCarrito;
import com.example.front_office.model.Producto;
import com.example.front_office.model.Usuario;
import com.example.front_office.repository.CarritoRepository;
import com.example.front_office.repository.ItemCarritoRepository; // Importar ItemCarritoRepository
import com.example.front_office.repository.ProductoRepository;
import com.example.front_office.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException; // Usar EntityNotFoundException
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
    @SuppressWarnings("unused")
    @Autowired private ItemCarritoRepository itemCarritoRepository; // Inyectar ItemCarritoRepository

    /**
     * Obtiene el carrito de un usuario. Si no existe, crea uno nuevo.
     * @param idUsuario ID del usuario.
     * @return El carrito del usuario.
     */
    @Transactional // Es transaccional porque podría crear un carrito nuevo
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Carrito obtenerCarritoPorUsuario(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + idUsuario));

        // Busca el carrito o crea uno nuevo si no existe
        return carritoRepository.findByUsuarioIdUsuario(idUsuario).orElseGet(() -> {
            Carrito nuevoCarrito = new Carrito();
            nuevoCarrito.setUsuario(usuario);
            nuevoCarrito.setFechaCreacion(new Date());
            return carritoRepository.save(nuevoCarrito); // Guarda el nuevo carrito
        });
    }


    /**
     * Agrega un producto al carrito de un usuario o actualiza la cantidad si ya existe.
     * @param idUsuario ID del usuario.
     * @param idProducto ID del producto a agregar.
     * @param cantidad Cantidad a agregar.
     * @return El carrito actualizado.
     */
    @Transactional
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Carrito agregarItem(Integer idUsuario, Integer idProducto, int cantidad) {
        // Validar cantidad positiva
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }

        Producto producto = productoRepository.findById(idProducto)
            .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + idProducto));

        // Obtener o crear carrito (usa el método que definimos antes)
        Carrito carrito = obtenerCarritoPorUsuario(idUsuario);

        // Buscar si el ítem ya existe en el carrito
        Optional<ItemCarrito> itemExistenteOpt = carrito.getItems().stream()
            .filter(item -> item.getProducto().getIdProducto().equals(idProducto))
            .findFirst();

        if (itemExistenteOpt.isPresent()) {
            // Si existe, actualiza cantidad y subtotal
            ItemCarrito item = itemExistenteOpt.get();
            int nuevaCantidad = item.getCantidad() + cantidad;
            // Validar stock (opcional pero recomendado)
            // if (nuevaCantidad > producto.getStock()) {
            //     throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
            // }
            item.setCantidad(nuevaCantidad);
            item.setSubtotal(producto.getPrecio().multiply(new BigDecimal(item.getCantidad())));
            // No es necesario guardar itemCarritoRepository explícitamente si la relación Cascade está bien configurada
        } else {
            // Si no existe, crea un nuevo ítem
             // Validar stock (opcional pero recomendado)
            // if (cantidad > producto.getStock()) {
            //     throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
            // }
            ItemCarrito nuevoItem = new ItemCarrito();
            nuevoItem.setCarrito(carrito); // Importante establecer la relación bidireccional
            nuevoItem.setProducto(producto);
            nuevoItem.setCantidad(cantidad);
            nuevoItem.setSubtotal(producto.getPrecio().multiply(new BigDecimal(cantidad)));
            carrito.getItems().add(nuevoItem); // Añadir a la lista en la entidad Carrito
        }
        // Guardar el carrito persistirá los cambios en los ítems debido al CascadeType.ALL
        return carritoRepository.save(carrito);
    }

    /**
     * Elimina un ítem específico del carrito de un usuario.
     * @param idUsuario ID del usuario propietario del carrito.
     * @param idItemCarrito ID del ItemCarrito a eliminar.
     * @return El carrito actualizado después de eliminar el ítem.
     * @throws EntityNotFoundException si el carrito o el ítem no se encuentran o no pertenecen al usuario.
     */
    @Transactional
    @SuppressWarnings({"rawtypes"})
    public Carrito eliminarItem(Integer idUsuario, Long idItemCarrito) {
        // Obtener el carrito del usuario (asegura que el carrito pertenece al usuario)
        Carrito carrito = obtenerCarritoPorUsuario(idUsuario); // Ya lanza excepción si el usuario no existe

        // Buscar el ítem dentro de la lista de ítems del carrito
        ItemCarrito itemAEliminar = carrito.getItems().stream()
                .filter(item -> item.getId().equals(idItemCarrito))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Ítem con ID " + idItemCarrito + " no encontrado en el carrito del usuario " + idUsuario));

        // Remover el ítem de la colección
        carrito.getItems().remove(itemAEliminar);

        // Como ItemCarrito tiene CascadeType.ALL y orphanRemoval=true en Carrito,
        // al guardar el carrito, JPA debería manejar la eliminación del ItemCarrito.
        // Si no, necesitarías llamar a itemCarritoRepository.delete(itemAEliminar);
        // Opcionalmente, podrías llamar a deleteById directamente si confías en que pertenece al usuario:
        // itemCarritoRepository.deleteById(idItemCarrito); // Asegúrate que las reglas de negocio lo permitan

        return carritoRepository.save(carrito); // Guardar el carrito actualizado
    }

     /**
     * Actualiza la cantidad de un ítem específico en el carrito.
     * Si la cantidad es 0 o menor, elimina el ítem.
     * @param idUsuario ID del usuario.
     * @param idItemCarrito ID del ItemCarrito a actualizar.
     * @param nuevaCantidad La nueva cantidad deseada.
     * @return El carrito actualizado.
     */
    @Transactional
    @SuppressWarnings("rawtypes")
    public Carrito actualizarCantidadItem(Integer idUsuario, Long idItemCarrito, int nuevaCantidad) {
        if (nuevaCantidad <= 0) {
            // Si la nueva cantidad es 0 o negativa, simplemente eliminamos el ítem
            return eliminarItem(idUsuario, idItemCarrito);
        }

        Carrito carrito = obtenerCarritoPorUsuario(idUsuario);

        ItemCarrito itemAActualizar = carrito.getItems().stream()
                .filter(item -> item.getId().equals(idItemCarrito))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Ítem con ID " + idItemCarrito + " no encontrado en el carrito del usuario " + idUsuario));

        Producto producto = itemAActualizar.getProducto();
        // Validar stock (opcional pero recomendado)
        // if (nuevaCantidad > producto.getStock()) {
        //     throw new RuntimeException("Stock insuficiente (" + producto.getStock() + ") para el producto: " + producto.getNombre());
        // }

        itemAActualizar.setCantidad(nuevaCantidad);
        itemAActualizar.setSubtotal(producto.getPrecio().multiply(new BigDecimal(nuevaCantidad)));

        return carritoRepository.save(carrito);
    }

}