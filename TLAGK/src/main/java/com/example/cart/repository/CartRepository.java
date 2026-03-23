package com.example.cart.repository;

import com.example.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // CHẮC CHẮN 100% LÀ DÒNG NÀY VẪN CÒN
    @Lock(LockModeType.PESSIMISTIC_WRITE) 
    Optional<Cart> findById(Long id);
}