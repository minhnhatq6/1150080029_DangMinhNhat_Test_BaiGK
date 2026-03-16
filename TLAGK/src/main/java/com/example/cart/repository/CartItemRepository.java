package com.example.cart.repository;

import com.example.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    CartItem findByCartIdAndProductId(Long cartId, Long productId);
    List<CartItem> findByCartId(Long cartId); 
}