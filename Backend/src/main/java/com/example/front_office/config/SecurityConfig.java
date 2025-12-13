package com.example.front_office.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
import java.util.List;

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
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // -----------------------------------------------------------
                        // 1. ZONA PÚBLICA (WhiteList)
                        // -----------------------------------------------------------
                        // ELIMINADO: .requestMatchers(toH2Console()).permitAll() ❌

                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/api/backoffice/auth/**").permitAll()
                        .requestMatchers("/api/pagos/crear_preferencia").permitAll()
                        // Documentación API (Swagger / OpenAPI)
                        .requestMatchers(
                                "/v2/api-docs",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-resources",
                                "/swagger-resources/**",
                                "/configuration/ui",
                                "/configuration/security",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Recursos estáticos
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/images/**").permitAll()

                        // Lectura pública de Catálogo
                        .requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categorias/**").permitAll()

                        // -----------------------------------------------------------
                        // 2. ZONA ADMIN (Back Office) - Requiere Rol ADMIN
                        // -----------------------------------------------------------
                        .requestMatchers(HttpMethod.POST, "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/categorias/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categorias/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categorias/**").hasRole("ADMIN")

                        // Endpoints exclusivos de gestión
                        .requestMatchers("/api/backoffice/**").hasRole("ADMIN")
                        .requestMatchers("/api/pedidos/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                        // -----------------------------------------------------------
                        // 3. ZONA USUARIOS (Clientes) - Requiere estar autenticado
                        // -----------------------------------------------------------
                        .requestMatchers("/api/carrito/**").authenticated()
                        .requestMatchers("/api/favoritos/**").authenticated()
                        .requestMatchers("/api/checkout/**").authenticated()
                        .requestMatchers("/api/perfil/**").authenticated()
                        .requestMatchers("/api/pedidos/**").authenticated()

                        // -----------------------------------------------------------
                        // 4. RESTO DEL MUNDO
                        // -----------------------------------------------------------
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // ELIMINADO: http.headers(...) frameOptions ya no es necesario sin H2 ❌

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control", "X-Requested-With", "Accept"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}