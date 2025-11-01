package com.example.front_office.repository;

import com.example.front_office.model.Favorito;
import com.example.front_office.model.Producto; // Importar Producto
import com.example.front_office.model.Usuario;   // Importar Usuario
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    // Encuentra todos los favoritos de un usuario específico
    List<Favorito> findByUsuarioIdUsuario(Integer idUsuario);

    // Encuentra un favorito específico por usuario y producto (para evitar duplicados o para eliminar)
    Optional<Favorito> findByUsuarioAndProducto(Usuario usuario, Producto producto);

    // Método para eliminar por usuario y producto directamente (alternativa)
    void deleteByUsuarioAndProducto(Usuario usuario, Producto producto);
}