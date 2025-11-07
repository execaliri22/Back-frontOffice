package com.example.front_office.config; // O tu paquete de configuración

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:./uploads/perfil}") // Debe coincidir con PerfilService
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path directorioUploadPath = Paths.get(uploadDir).toAbsolutePath();
        String location = "file:" + directorioUploadPath.toString() + "/";

        // Mapea la URL /uploads/perfil/** a la ubicación en el sistema de archivos
        registry.addResourceHandler("/uploads/perfil/**")
                .addResourceLocations(location);
    }
}