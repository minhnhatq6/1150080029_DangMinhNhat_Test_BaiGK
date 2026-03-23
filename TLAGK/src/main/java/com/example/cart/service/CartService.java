// package com.example.cart.service;


// import java.util.Optional;
// import com.example.cart.entity.*;
// import com.example.cart.repository.*;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.math.BigDecimal;

// @Service
// public class CartService {
//     @Autowired private CartRepository cartRepo;
//     @Autowired private CartItemRepository itemRepo;
//     @Autowired private ProductRepository productRepo;

//     @Transactional // Database transaction nhưng chưa có cơ chế Lock
//     public void addItemToCart(Long cartId, Long productId, int quantity) {
//         // 1. Lấy thông tin giỏ hàng và sản phẩm
//         Cart cart = cartRepo.findById(cartId).orElseThrow(() -> new RuntimeException("Cart not found"));
//         Product product = productRepo.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));

//         // 2. Cập nhật hoặc thêm mới CartItem
//         CartItem item = itemRepo.findByCartIdAndProductId(cartId, productId);
//         if (item != null) {
//             item.setQuantity(item.getQuantity() + quantity);
//         } else {
//             item = new CartItem();
//             item.setCartId(cartId);
//             item.setProductId(productId);
//             item.setQuantity(quantity);
//             item.setPrice(product.getPrice());
//         }
//         itemRepo.save(item);

//         // --- ĐOẠN CODE BỊ RACE CONDITION ---
//         // Nếu Thread A và Thread B cùng đọc biến cart ở trên cùng lúc
//         // Cả 2 sẽ lấy cùng 1 totalPrice cũ, cộng thêm tiền, và overwrite lên nhau ở đây.
//         BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
//         cart.setTotalPrice(cart.getTotalPrice().add(itemTotal));
        
//         cartRepo.save(cart);
//     }
//      @Transactional
//     public void removeItemFromCart(Long cartId, Long productId) {
//         // 1. Tìm CartItem cần xóa
//         Optional<CartItem> itemOpt = Optional.ofNullable(itemRepo.findByCartIdAndProductId(cartId, productId));

//         if (itemOpt.isPresent()) {
//             CartItem item = itemOpt.get();
//             Cart cart = cartRepo.findById(cartId).orElseThrow(() -> new RuntimeException("Cart not found"));

//             // 2. Tính toán số tiền cần trừ đi từ tổng giỏ hàng
//             // (Lấy giá lúc thêm * số lượng của item đó)
//             BigDecimal amountToSubtract = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

//             // --- ĐIỂM CÓ THỂ BỊ RACE CONDITION ---
//             // Cập nhật lại tổng tiền (Tương tự như lúc thêm)
//             cart.setTotalPrice(cart.getTotalPrice().subtract(amountToSubtract));
            
//             // 3. Xóa hẳn CartItem khỏi database
//             itemRepo.delete(item);
//             cartRepo.save(cart);
//         }
//         // Nếu không tìm thấy item thì không làm gì cả
//     }
// }
package com.example.cart.service;

import com.example.cart.entity.Cart;
import com.example.cart.entity.CartItem;
import com.example.cart.entity.Product;
import com.example.cart.repository.CartItemRepository;
import com.example.cart.repository.CartRepository;
import com.example.cart.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    @Autowired private CartRepository cartRepo;
    @Autowired private CartItemRepository itemRepo;
    @Autowired private ProductRepository productRepo;

    // 🔥 Dùng từ khóa 'synchronized' để ép các Request phải xếp hàng (Atomic)
    // Điều này thay thế cho Lock của Database vì SQLite không hỗ trợ.
    @Transactional
    public synchronized void addItemToCart(Long cartId, Long productId, int qty) {

        // 1. Đọc dữ liệu (Lúc này chỉ có 1 thread được vào đây nhờ 'synchronized')
        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem item = itemRepo.findByCartIdAndProductId(cartId, productId);

        if (item == null) {
            item = new CartItem();
            item.setCartId(cartId);
            item.setProductId(productId);
            item.setQuantity(0);
            item.setPrice(product.getPrice());
        }

        // 2. Cập nhật số lượng
        int newQty = item.getQuantity() + qty;
        if (newQty <= 0) {
            itemRepo.delete(item);
        } else {
            item.setQuantity(newQty);
            itemRepo.save(item);
        }

        // 3. Tính toán lại tổng tiền
        updateTotal(cart);
        
        // 🔥 Ép lưu xuống DB ngay lập tức trước khi nhả 'synchronized'
        cartRepo.saveAndFlush(cart); 
    }

    // Tương tự, thêm 'synchronized' cho các hàm thay đổi dữ liệu khác
    @Transactional
    public synchronized void removeItemFromCart(Long cartId, Long productId) {
        Cart cart = cartRepo.findById(cartId).orElseThrow(() -> new RuntimeException("Cart not found"));
        CartItem item = itemRepo.findByCartIdAndProductId(cartId, productId);
        if (item != null) {
            itemRepo.delete(item);
        }
        updateTotal(cart);
        cartRepo.saveAndFlush(cart);
    }

    private void updateTotal(Cart cart) {
        List<CartItem> items = itemRepo.findByCartId(cart.getId());
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem i : items) {
            BigDecimal itemTotal = i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity()));
            total = total.add(itemTotal);
        }
        cart.setTotalPrice(total);
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(Long cartId) {
        return itemRepo.findByCartId(cartId);
    }
}