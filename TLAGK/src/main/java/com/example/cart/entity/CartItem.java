package com.example.cart.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long cartId;
    private Long productId;
    private Integer quantity;
    private BigDecimal price;

    // --- CÁC HÀM GETTER / SETTER ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}