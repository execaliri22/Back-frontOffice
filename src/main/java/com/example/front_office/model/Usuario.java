package com.example.front_office.model;

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
    //agregar url para imagen
    private Integer idUsuario;
   
    private String nombre;
    @Column(unique = true)
    private String email;
    private String contrasenaHash;
    private String direccion;

    
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Carrito carrito;

    // Y AQUÍ TAMBIÉN 👇
    @OneToMany(mappedBy = "usuario")
    private List<Pedido> pedidos; // Debe ser List<Pedido>

    // --- Métodos de UserDetails (para Spring Security) ---
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