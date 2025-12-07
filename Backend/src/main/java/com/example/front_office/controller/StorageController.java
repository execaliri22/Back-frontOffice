package com.example.front_office.controller;

import com.example.front_office.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    // 1. Subir imagen (Para el Admin al crear producto)
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        String filename = storageService.store(file);

        // Generamos la URL completa para acceder a la imagen
        // Ej: http://localhost:8080/api/storage/filename.jpg
        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/storage/")
                .path(filename)
                .toUriString();

        return ResponseEntity.ok(Map.of("url", fileUrl));
    }

    // 2. Servir la imagen (Para que el navegador la pueda ver)
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) throws MalformedURLException {
        Path file = storageService.load(filename);
        Resource resource = new UrlResource(file.toUri());

        if (resource.exists() || resource.isReadable()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE) // O determina el tipo dinámicamente
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}