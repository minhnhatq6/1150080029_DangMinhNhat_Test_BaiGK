package com.example.cart.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Cart {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private BigDecimal totalPrice = BigDecimal.ZERO;

    // Getters / Setters
    public Long getId() { return id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}