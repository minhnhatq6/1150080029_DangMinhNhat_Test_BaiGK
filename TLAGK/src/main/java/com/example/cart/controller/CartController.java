// package com.example.cart.controller;

// import com.example.cart.entity.Cart;
// import com.example.cart.entity.CartItem;
// import com.example.cart.entity.Product;
// import com.example.cart.repository.CartItemRepository;
// import com.example.cart.repository.CartRepository;
// import com.example.cart.repository.ProductRepository;
// import com.example.cart.service.CartService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;

// @RestController
// @RequestMapping("/api/cart")
// public class CartController {
//     @Autowired private CartService cartService;
//     @Autowired private CartRepository cartRepo;
//     @Autowired private ProductRepository productRepo;
//     @Autowired private CartItemRepository itemRepo;

//     // 1. API Lấy toàn bộ sản phẩm của Shop
//     @GetMapping("/products")
//     public List<Product> getAllProducts() {
//         return productRepo.findAll();
//     }

//     // 2. API Lấy chi tiết các món trong giỏ hàng
//     @GetMapping("/{cartId}/items")
//     public List<CartItem> getCartItems(@PathVariable Long cartId) {
//         return itemRepo.findByCartId(cartId);
//     }

//     // 3. API Thêm/Bớt sản phẩm
//     @PostMapping("/{cartId}/add")
//     public String addProduct(@PathVariable Long cartId, @RequestParam Long productId, @RequestParam int qty) {
//         // Truyền qty = 1 là tăng, qty = -1 là giảm
//         cartService.addItemToCart(cartId, productId, qty);
//         return "Thành công!";
//     }

//     // 4. API Xem tổng tiền giỏ hàng
//     @GetMapping("/{cartId}")
//     public Cart getCart(@PathVariable Long cartId) {
//         return cartRepo.findById(cartId).orElse(null);
//     }
//     // 5. API Xóa một sản phẩm khỏi giỏ hàng
//     @PostMapping("/{cartId}/remove")
//     public String removeProduct(@PathVariable Long cartId, @RequestParam Long productId) {
//         cartService.removeItemFromCart(cartId, productId);
//         return "Đã xóa thành công!";
//     }
// }

package com.example.cart.controller;

import com.example.cart.entity.Cart;
import com.example.cart.entity.CartItem;
import com.example.cart.entity.Product;
import com.example.cart.repository.CartItemRepository;
import com.example.cart.repository.CartRepository;
import com.example.cart.repository.ProductRepository;
import com.example.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired private CartService cartService;
    @Autowired private CartRepository cartRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private CartItemRepository itemRepo;

    // 1. API Lấy toàn bộ sản phẩm của Shop
    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    // 2. API Lấy chi tiết các món trong giỏ hàng
    // SỬA LẠI HÀM NÀY
    @GetMapping("/{cartId}/items")
    public List<CartItem> getCartItems(@PathVariable Long cartId) {
        // Thay vì: return itemRepo.findByCartId(cartId);
        // Sửa thành:
        return cartService.getCartItems(cartId);
    }

    // 3. API Thêm/Bớt sản phẩm
    @PostMapping("/{cartId}/add")
    public String addProduct(@PathVariable Long cartId, @RequestParam Long productId, @RequestParam int qty) {
        // Truyền qty = 1 là tăng, qty = -1 là giảm
        cartService.addItemToCart(cartId, productId, qty);
        return "Thành công!";
    }

    // 4. API Xem tổng tiền giỏ hàng
    @GetMapping("/{cartId}")
    public Cart getCart(@PathVariable Long cartId) {
        return cartRepo.findById(cartId).orElse(null);
    }
    // 5. API Xóa một sản phẩm khỏi giỏ hàng
    @PostMapping("/{cartId}/remove")
    public String removeProduct(@PathVariable Long cartId, @RequestParam Long productId) {
        cartService.removeItemFromCart(cartId, productId);
        return "Đã xóa thành công!";
    }
}