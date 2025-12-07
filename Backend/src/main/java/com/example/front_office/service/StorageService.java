package com.example.front_office.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageService {

    // Carpeta donde se guardarán las fotos (se crea en la raíz del proyecto)
    private final Path rootLocation = Paths.get("uploads");

    // Constructor: Crea la carpeta si no existe al arrancar
    public StorageService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar la carpeta de uploads", e);
        }
    }

    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Error: archivo vacío.");
            }

            // Generar nombre único para evitar conflictos (ej: "foto.jpg" -> "uuid-foto.jpg")
            String filename = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

            Path destinationFile = this.rootLocation.resolve(
                            Paths.get(filename))
                    .normalize().toAbsolutePath();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return filename; // Devolvemos el nombre para guardarlo en la BD
        } catch (IOException e) {
            throw new RuntimeException("Fallo al guardar archivo", e);
        }
    }
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }
}