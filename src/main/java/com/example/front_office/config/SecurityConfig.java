package com.example.front_office.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(withDefaults()) // Activa CORS usando la configuración del Bean corsConfigurationSource
            .csrf(AbstractHttpConfigurer::disable) // Deshabilita CSRF
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll() // Permite acceso público a /auth/**
                .requestMatchers(toH2Console()).permitAll() // Permite acceso público a la consola H2
                .requestMatchers("/api/productos/**").permitAll() // Permite acceso público a /api/productos/**
                .requestMatchers("/api/categorias/**").permitAll() // Permite acceso público a /api/categorias/**
                .requestMatchers("/favoritos/**").authenticated() // Requiere autenticación para /favoritos/**
                // Asegúrate de incluir otras rutas que requieran autenticación
                .requestMatchers("/carrito/**").authenticated()
                .requestMatchers("/checkout/**").authenticated()
                .anyRequest().authenticated() // Cualquier otra petición requiere autenticación
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Usa sesiones sin estado
            .authenticationProvider(authenticationProvider) // Configura el proveedor de autenticación
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // Añade el filtro JWT

        // Permite que la consola H2 se muestre en iframes
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }

    /**
     * Define la configuración CORS para la aplicación.
     * @return CorsConfigurationSource con las reglas CORS definidas.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Especifica los orígenes permitidos (tu frontend Angular)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));

        // Especifica los métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Especifica las cabeceras permitidas
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control"));

        // Permite que el navegador envíe credenciales
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica esta configuración a todas las rutas ("/**")
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}