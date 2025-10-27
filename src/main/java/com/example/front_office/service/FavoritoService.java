package com.example.front_office.service;

import com.example.front_office.model.Favorito;
import com.example.front_office.model.Producto;
import com.example.front_office.model.Usuario;
import com.example.front_office.repository.FavoritoRepository;
import com.example.front_office.repository.ProductoRepository;
import com.example.front_office.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FavoritoService {

    @Autowired private FavoritoRepository favoritoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ProductoRepository productoRepository;

    /**
     * Obtiene la lista de favoritos de un usuario.
     * @param idUsuario ID del usuario.
     * @return Lista de entidades Favorito (que incluyen el producto).
     */
    @Transactional(readOnly = true) // Transacción de solo lectura
    @SuppressWarnings("rawtypes")
    public List<Favorito> obtenerFavoritosPorUsuario(Integer idUsuario) {
        // Verifica que el usuario exista (aunque el controlador ya lo hace)
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new EntityNotFoundException("Usuario no encontrado con ID: " + idUsuario);
        }
        return favoritoRepository.findByUsuarioIdUsuario(idUsuario);
    }

    /**
     * Agrega un producto a la lista de favoritos de un usuario.
     * No agrega si ya existe.
     * @param idUsuario ID del usuario.
     * @param idProducto ID del producto.
     * @return El objeto Favorito creado (o el existente si ya estaba).
     */
    @Transactional
    @SuppressWarnings("rawtypes")
    public Favorito agregarFavorito(Integer idUsuario, Integer idProducto) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + idUsuario));
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + idProducto));

        // Verifica si ya es favorito para no duplicar
        Optional<Favorito> existente = favoritoRepository.findByUsuarioAndProducto(usuario, producto);
        if (existente.isPresent()) {
            return existente.get(); // Ya existe, devuelve el existente
        }

        // Crea y guarda el nuevo favorito
        Favorito nuevoFavorito = new Favorito(null, usuario, producto);
        return favoritoRepository.save(nuevoFavorito);
    }

    /**
     * Elimina un producto de la lista de favoritos de un usuario.
     * @param idUsuario ID del usuario.
     * @param idProducto ID del producto a eliminar.
     */
    @Transactional
    @SuppressWarnings("rawtypes")
    public void eliminarFavorito(Integer idUsuario, Integer idProducto) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + idUsuario));
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + idProducto));

        // Elimina directamente usando el método del repositorio
        favoritoRepository.deleteByUsuarioAndProducto(usuario, producto);
        // Nota: Esto no lanzará error si no existía, simplemente no hará nada.
        // Si necesitaras confirmar que existía, podrías buscarlo primero:
        // Optional<Favorito> existente = favoritoRepository.findByUsuarioAndProducto(usuario, producto);
        // if (existente.isEmpty()) {
        //     throw new EntityNotFoundException("Favorito no encontrado para eliminar.");
        // }
        // favoritoRepository.delete(existente.get());
    }
}