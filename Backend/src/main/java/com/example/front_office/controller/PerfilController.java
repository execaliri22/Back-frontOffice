package com.example.front_office.controller;

import com.example.front_office.model.Usuario;
import com.example.front_office.repository.UsuarioRepository; // Para obtener el ID del usuario actual
import com.example.front_office.service.PerfilService;
import com.example.front_office.service.AuthService; // Para potencialmente devolver un nuevo token
import com.example.front_office.service.JwtService;   // Para potencialmente generar un nuevo token
import com.example.front_office.controller.dto.AuthResponse; // Para devolver nuevo token

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// DTOs locales para las peticiones de cambio
@SuppressWarnings("unused")
record NombreUpdateRequest(String nombre) {}
record ContrasenaUpdateRequest(String actual, String nueva) {}

@RestController
@RequestMapping("/api/perfil") // Ruta base para el perfil
public class PerfilController {

    @Autowired
    private PerfilService perfilService;

    @Autowired
    private UsuarioRepository usuarioRepository; // Para obtener ID de usuario

    @Autowired
    private JwtService jwtService; // Para generar un nuevo token si es necesario

    private Integer idUsuario;


    // Método auxiliar para obtener el ID del usuario actual
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

    // Genera un nuevo token con los datos actualizados del usuario
    private String generateUpdatedToken(Usuario usuario) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("nombre", usuario.getNombre());
        if (usuario.getFotoPerfilUrl() != null) {
            extraClaims.put("fotoPerfilUrl", usuario.getFotoPerfilUrl());
        }
        // Asegúrate de que tu JwtService pueda generar el token a partir de un objeto Usuario
        // que implemente UserDetails
        return jwtService.generateToken(extraClaims, usuario);
    }


    // GET /api/perfil/me - Obtener datos del perfil del usuario logueado
    @GetMapping("/me")
    public ResponseEntity<?> obtenerMiPerfil() {
        try {
            Integer idUsuario = getCurrentUserId();
            Usuario usuario = perfilService.obtenerPerfilUsuario(idUsuario);
            // Podrías devolver un DTO específico aquí si no quieres exponer toda la entidad Usuario
            return ResponseEntity.ok(usuario);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener el perfil: " + e.getMessage());
        }
    }

    // PUT /api/perfil/nombre - Actualizar nombre
    @PutMapping("/nombre")
    public ResponseEntity<?> actualizarNombre(@RequestBody NombreUpdateRequest request) {
        try {
            Integer idUsuario = getCurrentUserId();
            if (request.nombre() == null || request.nombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El nombre no puede estar vacío.");
            }
            Usuario usuarioActualizado = perfilService.actualizarNombreUsuario(idUsuario, request.nombre().trim());

            // Devolver el nuevo token con el nombre actualizado
            String nuevoToken = generateUpdatedToken(usuarioActualizado);
            return ResponseEntity.ok(new AuthResponse(nuevoToken));

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar el nombre: " + e.getMessage());
        }
    }

    // PUT /api/perfil/contrasena - Cambiar contraseña
    @PutMapping("/contrasena")
    public ResponseEntity<?> cambiarContrasena(@RequestBody ContrasenaUpdateRequest request) {
        try {
            Integer idUsuario = getCurrentUserId();
            perfilService.cambiarContrasena(idUsuario, request.actual(), request.nueva());
            return ResponseEntity.ok().body("Contraseña actualizada correctamente."); // No es necesario nuevo token aquí
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) { // Captura error de contraseña incorrecta o inválida
             return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cambiar la contraseña: " + e.getMessage());
        }
    }

    // POST /api/perfil/foto - Subir/Actualizar foto de perfil
    // Requiere configuración de Multipart en Spring Boot (generalmente habilitada por defecto)
    @PostMapping("/foto")
    public ResponseEntity<?> actualizarFoto(@RequestParam("file") MultipartFile file) {
        try {
            Integer idUsuario = getCurrentUserId();
            Usuario usuarioActualizado = perfilService.actualizarFotoPerfil(idUsuario, file);

             // Devolver el nuevo token con la URL de la foto actualizada
            String nuevoToken = generateUpdatedToken(usuarioActualizado);
            return ResponseEntity.ok(new AuthResponse(nuevoToken));

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
             return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar la foto: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error inesperado al actualizar la foto: " + e.getMessage());
        }
    }

    // DELETE /api/perfil/foto - Eliminar foto de perfil
    @DeleteMapping("/foto")
    public ResponseEntity<?> eliminarFoto() {
        try {
            Integer idUsuario = getCurrentUserId();
            Usuario usuarioActualizado = perfilService.eliminarFotoPerfil(idUsuario);

            // Devolver el nuevo token sin la URL de la foto
            String nuevoToken = generateUpdatedToken(usuarioActualizado);
            return ResponseEntity.ok(new AuthResponse(nuevoToken));

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IOException e) {
            // Loggear pero no necesariamente fallar si el archivo ya no existía
            System.err.println("Advertencia al eliminar foto de perfil: " + e.getMessage());
            
             // Aún así, actualizamos la BD y devolvemos éxito
             Usuario usuario = perfilService.obtenerPerfilUsuario(idUsuario); // Reobtener por si acaso
             String nuevoToken = generateUpdatedToken(usuario);
             return ResponseEntity.ok(new AuthResponse(nuevoToken));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar la foto: " + e.getMessage());
        }
    }
}