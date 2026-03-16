package com.example.cart.controller;

import com.example.cart.entity.*;
import com.example.cart.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class InitData {
    @Autowired private ProductRepository productRepo;
    @Autowired private CartRepository cartRepo;

    @PostConstruct
    public void init() {
        if (productRepo.count() == 0) {
            productRepo.save(new Product("Laptop Dell", new BigDecimal("1000")));
            productRepo.save(new Product("Mouse Logitech", new BigDecimal("50")));
            
            Cart cart = new Cart();
            cart.setUserId(1L);
            cartRepo.save(cart); // Sẽ tạo ra Cart có ID = 1
        }
    }
}