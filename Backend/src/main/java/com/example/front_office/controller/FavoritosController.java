package com.example.front_office.controller;

import com.example.front_office.model.Favorito;
import com.example.front_office.model.Usuario; // Importar Usuario
import com.example.front_office.repository.UsuarioRepository; // Importar UsuarioRepository
import com.example.front_office.service.FavoritoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // Para obtener usuario autenticado
import org.springframework.security.core.context.SecurityContextHolder; // Para obtener usuario autenticado
import org.springframework.security.core.userdetails.UserDetails; // Para obtener usuario autenticado
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Para mapear si fuera necesario

@SuppressWarnings("unused")
@RestController
@RequestMapping("/favoritos") // Ruta base para favoritos
public class FavoritosController {

    @Autowired private FavoritoService favoritoService;
    @Autowired private UsuarioRepository usuarioRepository; // Para buscar usuario por email

    // Método auxiliar para obtener el ID del usuario actual (igual que en CarritoController)
    @SuppressWarnings("rawtypes")
    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new RuntimeException("Usuario no autenticado.");
        }
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(userEmail);
        if (usuarioOpt.isEmpty()) {
             throw new RuntimeException("Usuario autenticado no encontrado en la base de datos.");
        }
        return usuarioOpt.get().getIdUsuario();
    }


    /**
     * Endpoint GET para obtener la lista de favoritos del usuario autenticado.
     * URL: GET http://localhost:8080/favoritos
     */
    @GetMapping
    public ResponseEntity<?> obtenerMisFavoritos() {
        try {
            Integer idUsuario = getCurrentUserId();
            @SuppressWarnings("rawtypes") // Si Favorito tiene tipos genéricos no especificados
            List<Favorito> favoritos = favoritoService.obtenerFavoritosPorUsuario(idUsuario);
            // Devuelve la lista directamente. Cada 'Favorito' incluirá el objeto 'Producto'.
            return ResponseEntity.ok(favoritos);
        } catch (RuntimeException e) { // Captura error de autenticación u otro
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener favoritos: " + e.getMessage());
        }
    }


    /**
     * Endpoint POST para agregar un producto a favoritos.
     * URL: POST http://localhost:8080/favoritos/{idProducto}
     * donde {idProducto} es el ID del producto a agregar.
     */
    @PostMapping("/{idProducto}")
    public ResponseEntity<?> agregarAFavoritos(@PathVariable Integer idProducto) {
       try {
            Integer idUsuario = getCurrentUserId();
            @SuppressWarnings("rawtypes") // Si Favorito tiene tipos genéricos no especificados
            Favorito favorito = favoritoService.agregarFavorito(idUsuario, idProducto);
            // Devuelve 201 Created con el favorito agregado (o existente)
            return ResponseEntity.status(HttpStatus.CREATED).body(favorito);
       } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // Producto o Usuario no encontrado
       } catch (RuntimeException e) { // Captura error de autenticación u otro
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
       } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al agregar favorito: " + e.getMessage());
       }
    }


    /**
     * Endpoint DELETE para eliminar un producto de favoritos.
     * URL: DELETE http://localhost:8080/favoritos/{idProducto}
     * donde {idProducto} es el ID del producto a eliminar.
     */
    @DeleteMapping("/{idProducto}")
    public ResponseEntity<?> eliminarDeFavoritos(@PathVariable Integer idProducto) {
        try {
            Integer idUsuario = getCurrentUserId();
            favoritoService.eliminarFavorito(idUsuario, idProducto);
            // Devuelve 204 No Content si se eliminó correctamente (o si no existía)
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            // Podría ocurrir si el usuario o producto no existen (aunque eliminar no falle si el favorito no existe)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) { // Captura error de autenticación u otro
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el favorito: " + e.getMessage());
        }
    }
}