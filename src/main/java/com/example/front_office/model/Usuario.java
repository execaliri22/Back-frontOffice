package com.example.front_office.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set; // Importar Set si usas la relación con Favorito

@Entity
@Data
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUsuario;

    private String nombre;

    @Column(unique = true, nullable = false) // Email debe ser único y no nulo
    private String email;

    @JsonIgnore // No incluir hash de contraseña en respuestas JSON
    private String contrasenaHash;

    private String direccion;

    private String fotoPerfilUrl; // <-- NUEVO CAMPO

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("usuario-carrito") // Lado principal para serialización JSON (evita bucles)
    private Carrito carrito;

    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
    @JsonManagedReference("usuario-pedidos") // Lado principal para serialización JSON
    private List<Pedido> pedidos;

    // Relación Opcional con Favoritos (añadida en la implementación anterior)
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore // Ignorar al serializar Usuario para evitar bucles
    private Set<Favorito> favoritos;

    // --- Métodos de UserDetails ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Devuelve null si no usas roles/autoridades específicas
        return null;
    }

    @Override
    public String getPassword() {
        // Devuelve el hash de la contraseña almacenado
        return this.contrasenaHash;
    }

    @Override
    public String getUsername() {
        // Usa el email como nombre de usuario para Spring Security
        return this.email;
    }

    // Los siguientes métodos pueden devolver true si no implementas lógica de expiración/bloqueo
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Podrías tener un campo 'activo' en la BD y devolver su valor aquí
        return true;
    }
}