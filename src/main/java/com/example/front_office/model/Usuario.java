package com.example.front_office.model;

import com.fasterxml.jackson.annotation.JsonManagedReference; // <-- IMPORTAR
// Ignora la contraseña al serializar a JSON por seguridad
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Data
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUsuario;

    private String nombre;
    @Column(unique = true)
    private String email;

    @JsonIgnore // <-- IGNORAR contrasenaHash en respuestas JSON
    private String contrasenaHash;
    private String direccion;


    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // Considera LAZY fetching
    @JsonManagedReference("usuario-carrito") // <-- Lado "principal" que se serializa
    private Carrito carrito;


    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY) // Considera LAZY fetching
    @JsonManagedReference("usuario-pedidos") // <-- Lado "principal" que se serializa
    private List<Pedido> pedidos;

    // --- Métodos de UserDetails ---
    // (Sin cambios aquí)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return null; }
    @Override
    public String getPassword() { return this.contrasenaHash; }
    @Override
    public String getUsername() { return this.email; }
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}