package com.example.cart;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.cart.entity.Cart;
import com.example.cart.entity.CartItem;
import com.example.cart.entity.Product;
import com.example.cart.repository.CartRepository;
import com.example.cart.repository.ProductRepository;
import com.example.cart.service.CartService;

@SpringBootTest
public class CartServiceWhiteBoxTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepo;

    @Autowired
    private ProductRepository productRepo;

    private Cart cart;
    private Product laptop;
    private Product mouse;

    @BeforeEach
    void setup() {

        cart = new Cart();
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepo.save(cart);

        laptop = new Product();
        laptop.setPrice(new BigDecimal("1000"));
        productRepo.save(laptop);

        mouse = new Product();
        mouse.setPrice(new BigDecimal("50"));
        productRepo.save(mouse);
    }

    // TC001
    @Test
    void TC001_addLaptop() {

        cartService.addItemToCart(cart.getId(), laptop.getId(), 1);

        Cart updated = cartRepo.findById(cart.getId()).get();

        assertEquals(new BigDecimal("1000"), updated.getTotalPrice());
    }

    // TC002
    @Test
    void TC002_addLaptopTwice() {

        cartService.addItemToCart(cart.getId(), laptop.getId(), 1);
        cartService.addItemToCart(cart.getId(), laptop.getId(), 1);

        Cart updated = cartRepo.findById(cart.getId()).get();

        assertEquals(new BigDecimal("2000"), updated.getTotalPrice());
    }

    // TC003
    @Test
    void TC003_addMouse() {

        cartService.addItemToCart(cart.getId(), mouse.getId(), 1);

        Cart updated = cartRepo.findById(cart.getId()).get();

        assertEquals(new BigDecimal("50"), updated.getTotalPrice());
    }

    // TC004
    @Test
    void TC004_addLaptopAndMouse() {

        cartService.addItemToCart(cart.getId(), laptop.getId(), 1);
        cartService.addItemToCart(cart.getId(), mouse.getId(), 1);

        Cart updated = cartRepo.findById(cart.getId()).get();

        assertEquals(new BigDecimal("1050"), updated.getTotalPrice());
    }

    // TC005
    @Test
    void TC005_removeItem() {

        cartService.addItemToCart(cart.getId(), laptop.getId(), 1);
        cartService.removeItemFromCart(cart.getId(), laptop.getId());

        Cart updated = cartRepo.findById(cart.getId()).get();

        assertEquals(BigDecimal.ZERO, updated.getTotalPrice());
    }

    // TC006
    @Test
    void TC006_addMultipleItems() {

        cartService.addItemToCart(cart.getId(), laptop.getId(), 2);
        cartService.addItemToCart(cart.getId(), mouse.getId(), 1);

        Cart updated = cartRepo.findById(cart.getId()).get();

        assertEquals(new BigDecimal("2050"), updated.getTotalPrice());
    }
}