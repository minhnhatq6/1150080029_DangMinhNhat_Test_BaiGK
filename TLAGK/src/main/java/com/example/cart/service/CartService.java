package com.example.cart.service;


import java.util.Optional;
import com.example.cart.entity.*;
import com.example.cart.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CartService {
    @Autowired private CartRepository cartRepo;
    @Autowired private CartItemRepository itemRepo;
    @Autowired private ProductRepository productRepo;

    @Transactional // Database transaction nhưng chưa có cơ chế Lock
    public void addItemToCart(Long cartId, Long productId, int quantity) {
        // 1. Lấy thông tin giỏ hàng và sản phẩm
        Cart cart = cartRepo.findById(cartId).orElseThrow(() -> new RuntimeException("Cart not found"));
        Product product = productRepo.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));

        // 2. Cập nhật hoặc thêm mới CartItem
        CartItem item = itemRepo.findByCartIdAndProductId(cartId, productId);
        if (item != null) {
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            item = new CartItem();
            item.setCartId(cartId);
            item.setProductId(productId);
            item.setQuantity(quantity);
            item.setPrice(product.getPrice());
        }
        itemRepo.save(item);

        // --- ĐOẠN CODE BỊ RACE CONDITION ---
        // Nếu Thread A và Thread B cùng đọc biến cart ở trên cùng lúc
        // Cả 2 sẽ lấy cùng 1 totalPrice cũ, cộng thêm tiền, và overwrite lên nhau ở đây.
        BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        cart.setTotalPrice(cart.getTotalPrice().add(itemTotal));
        
        cartRepo.save(cart);
    }
     @Transactional
    public void removeItemFromCart(Long cartId, Long productId) {
        // 1. Tìm CartItem cần xóa
        Optional<CartItem> itemOpt = Optional.ofNullable(itemRepo.findByCartIdAndProductId(cartId, productId));

        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            Cart cart = cartRepo.findById(cartId).orElseThrow(() -> new RuntimeException("Cart not found"));

            // 2. Tính toán số tiền cần trừ đi từ tổng giỏ hàng
            // (Lấy giá lúc thêm * số lượng của item đó)
            BigDecimal amountToSubtract = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            // --- ĐIỂM CÓ THỂ BỊ RACE CONDITION ---
            // Cập nhật lại tổng tiền (Tương tự như lúc thêm)
            cart.setTotalPrice(cart.getTotalPrice().subtract(amountToSubtract));
            
            // 3. Xóa hẳn CartItem khỏi database
            itemRepo.delete(item);
            cartRepo.save(cart);
        }
        // Nếu không tìm thấy item thì không làm gì cả
    }
}