package com.example.front_office.model;

import jakarta.persistence.*;

@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación Many-to-One: Muchos ítems pueden pertenecer a una Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id") // Nombre de la columna en la BD
    private Order order; // La Order a la que pertenece este ítem

    private Long productId; // ID del producto de tu inventario
    private Integer quantity;
    private Double price; // Precio unitario en el momento de la compra

    // --- Constructor y Getters/Setters (Añadir aquí) ---
    // (O usar Lombok para simplificar)

    // Constructor vacío (necesario para JPA)
    public OrderItem() {
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}