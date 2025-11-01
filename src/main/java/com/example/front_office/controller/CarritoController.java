package com.example.front_office.controller;

import com.example.front_office.model.Carrito;
import com.example.front_office.service.CarritoService;
import jakarta.persistence.EntityNotFoundException; // Importar
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // Para obtener usuario autenticado
import org.springframework.security.core.context.SecurityContextHolder; // Para obtener usuario autenticado
import org.springframework.security.core.userdetails.UserDetails; // Para obtener usuario autenticado
import org.springframework.web.bind.annotation.*;
import com.example.front_office.model.Usuario; // Importar Usuario
import com.example.front_office.repository.UsuarioRepository; // Importar UsuarioRepository
import java.util.Optional;


// DTOs locales (puedes moverlos a su propio archivo si prefieres)
record AddItemRequest(Integer idProducto, int cantidad) {}
record UpdateQuantityRequest(int cantidad) {}

@RestController
@RequestMapping("/carrito") // Ruta base sigue siendo /carrito
public class CarritoController {

    @Autowired private CarritoService carritoService;
    @Autowired private UsuarioRepository usuarioRepository; // Para buscar usuario por email

    /**
     * Obtiene el ID del usuario autenticado actualmente a partir del token JWT.
     * @return El ID del usuario.
     * @throws RuntimeException si no se encuentra el usuario o no está autenticado.
     */
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
     * Endpoint GET para obtener el carrito del usuario autenticado.
     * URL: GET http://localhost:8080/carrito
     */
    @GetMapping
    @SuppressWarnings("rawtypes")
    public ResponseEntity<?> obtenerCarrito() {
        try {
            Integer idUsuario = getCurrentUserId(); // Obtiene ID del usuario del token
            Carrito carrito = carritoService.obtenerCarritoPorUsuario(idUsuario);
            return ResponseEntity.ok(carrito);
        } catch (EntityNotFoundException e) {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) { // Captura error de autenticación u otro
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener el carrito: " + e.getMessage());
        }
    }


    /**
     * Endpoint POST para agregar un ítem al carrito del usuario autenticado.
     * URL: POST http://localhost:8080/carrito
     * Body: { "idProducto": 1, "cantidad": 2 }
     */
    @PostMapping
    @SuppressWarnings("rawtypes")
    public ResponseEntity<?> agregarAlCarrito(@RequestBody AddItemRequest request) {
       try {
            Integer idUsuario = getCurrentUserId(); // Obtiene ID del usuario del token
            Carrito carrito = carritoService.agregarItem(idUsuario, request.idProducto(), request.cantidad());
            return ResponseEntity.ok(carrito); // 200 OK con carrito actualizado
       } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // Producto no encontrado
       } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // Cantidad inválida
       } catch (RuntimeException e) { // Captura error de autenticación u otro
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
       } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al agregar ítem: " + e.getMessage());
       }
    }

    /**
     * Endpoint PUT para actualizar la cantidad de un ítem en el carrito.
     * URL: PUT http://localhost:8080/carrito/items/{idItemCarrito}
     * Body: { "cantidad": 3 }
     * Si la cantidad es <= 0, el ítem se eliminará.
     */
    @PutMapping("/items/{idItemCarrito}")
    @SuppressWarnings("rawtypes")
    public ResponseEntity<?> actualizarCantidadItem(
            @PathVariable Long idItemCarrito,
            @RequestBody UpdateQuantityRequest request) {
        try {
            Integer idUsuario = getCurrentUserId();
            Carrito carrito = carritoService.actualizarCantidadItem(idUsuario, idItemCarrito, request.cantidad());
             if (request.cantidad() <= 0) {
                 // Si se eliminó porque cantidad era <= 0, devolvemos No Content como en DELETE
                 return ResponseEntity.noContent().build();
             } else {
                 return ResponseEntity.ok(carrito); // 200 OK con carrito actualizado
             }
        } catch (EntityNotFoundException e) {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // Ítem o carrito no encontrado
        } catch (RuntimeException e) { // Captura error de autenticación u otro
              return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar cantidad: " + e.getMessage());
        }
    }


    /**
     * Endpoint DELETE para eliminar un ítem específico del carrito del usuario autenticado.
     * URL: DELETE http://localhost:8080/carrito/items/{idItemCarrito}
     * donde {idItemCarrito} es el ID del *ItemCarrito*, no del Producto.
     */
    @DeleteMapping("/items/{idItemCarrito}")
    public ResponseEntity<?> eliminarItemDelCarrito(@PathVariable Long idItemCarrito) {
        try {
            Integer idUsuario = getCurrentUserId(); // Obtiene ID del usuario del token
            carritoService.eliminarItem(idUsuario, idItemCarrito);
            // Devuelve 204 No Content si se eliminó correctamente
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            // Devuelve 404 Not Found si el ítem no se encontró en el carrito del usuario
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) { // Captura error de autenticación u otro
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el ítem: " + e.getMessage());
        }
    }
}