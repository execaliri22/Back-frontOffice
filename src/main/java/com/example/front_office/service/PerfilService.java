package com.example.front_office.service;

import com.example.front_office.model.Usuario;
import com.example.front_office.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class PerfilService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- Configuración para guardar archivos localmente (EJEMPLO, NO PARA PRODUCCIÓN REAL) ---
    // Deberías usar variables de entorno o application.properties
    @Value("${file.upload-dir:./uploads/perfil}") // Directorio donde se guardarán las fotos
    private String uploadDir;
    // --- Fin Configuración Ejemplo ---

    @Transactional(readOnly = true)
    public Usuario obtenerPerfilUsuario(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + idUsuario));
    }

    @Transactional
    public Usuario actualizarNombreUsuario(Integer idUsuario, String nuevoNombre) {
        Usuario usuario = obtenerPerfilUsuario(idUsuario); // Reutiliza el método para obtener y validar
        usuario.setNombre(nuevoNombre);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void cambiarContrasena(Integer idUsuario, String contrasenaActual, String contrasenaNueva) {
        Usuario usuario = obtenerPerfilUsuario(idUsuario);

        // Verificar la contraseña actual
        if (!passwordEncoder.matches(contrasenaActual, usuario.getContrasenaHash())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta.");
        }

        // Validar la nueva contraseña (puedes añadir más reglas)
        if (contrasenaNueva == null || contrasenaNueva.length() < 6) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres.");
        }

        // Actualizar el hash de la contraseña
        usuario.setContrasenaHash(passwordEncoder.encode(contrasenaNueva));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizarFotoPerfil(Integer idUsuario, MultipartFile archivoFoto) throws IOException {
        Usuario usuario = obtenerPerfilUsuario(idUsuario);

        // --- Lógica de ejemplo para guardar archivo localmente ---
        if (archivoFoto == null || archivoFoto.isEmpty()) {
            throw new IllegalArgumentException("El archivo de la foto no puede estar vacío.");
        }

        // Validar tipo de archivo (ejemplo básico)
        String contentType = archivoFoto.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/gif"))) {
            throw new IllegalArgumentException("Formato de archivo no soportado. Use JPG, PNG o GIF.");
        }

        // Crear directorio si no existe
        Path directorioUpload = Paths.get(uploadDir);
        if (!Files.exists(directorioUpload)) {
            Files.createDirectories(directorioUpload);
        }

        // Generar un nombre de archivo único
        String extension = obtenerExtension(archivoFoto.getOriginalFilename());
        String nombreArchivo = UUID.randomUUID().toString() + extension;
        Path rutaArchivo = directorioUpload.resolve(nombreArchivo);

        // Copiar el archivo al directorio de destino
        Files.copy(archivoFoto.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

        // Construir la URL relativa para guardar en la BD (ajusta según cómo sirvas los archivos)
        // ESTO ES SOLO UN EJEMPLO. Necesitas configurar Spring para servir estos archivos estáticos
        // o usar una URL absoluta si se sirven desde otro lugar (CDN, S3, etc.)
        String urlFoto = "/uploads/perfil/" + nombreArchivo;

        // Eliminar foto anterior si existe (opcional, depende de tu lógica de almacenamiento)
        // eliminarArchivoFoto(usuario.getFotoPerfilUrl()); // Implementar esta función si es necesario

        usuario.setFotoPerfilUrl(urlFoto);
        return usuarioRepository.save(usuario);
        // --- Fin Lógica de ejemplo ---
    }

    @Transactional
    public Usuario eliminarFotoPerfil(Integer idUsuario) throws IOException {
        Usuario usuario = obtenerPerfilUsuario(idUsuario);

        // Eliminar archivo físico (ejemplo local)
        eliminarArchivoFoto(usuario.getFotoPerfilUrl());

        usuario.setFotoPerfilUrl(null); // Establecer la URL a null en la BD
        return usuarioRepository.save(usuario);
    }

    // --- Métodos auxiliares para manejo de archivos (ejemplo local) ---
    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.lastIndexOf('.') == -1) {
            return ""; // Sin extensión o nombre inválido
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf('.'));
    }

    private void eliminarArchivoFoto(String urlFotoRelativa) throws IOException {
        if (urlFotoRelativa == null || urlFotoRelativa.isEmpty()) {
            return; // No hay foto para eliminar
        }
        // Asume que la URL guardada es relativa al directorio base de uploads
        // ¡CUIDADO! Esta lógica es muy básica y podría ser insegura si la URL no es lo esperado.
        try {
            String nombreArchivo = urlFotoRelativa.substring(urlFotoRelativa.lastIndexOf('/') + 1);
            Path rutaArchivo = Paths.get(uploadDir).resolve(nombreArchivo);
            if (Files.exists(rutaArchivo)) {
                Files.delete(rutaArchivo);
            }
        } catch (Exception e) {
            // Loggear el error, pero no necesariamente fallar la operación de BD
            System.err.println("Error al eliminar archivo físico de foto de perfil: " + e.getMessage());
        }
    }
    // --- Fin Métodos auxiliares ---

}