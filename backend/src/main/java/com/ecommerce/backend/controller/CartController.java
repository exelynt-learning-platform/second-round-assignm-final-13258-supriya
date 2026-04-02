package com.ecommerce.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.backend.dto.request.CartItemRequest;
import com.ecommerce.backend.dto.response.CartResponse;
import com.ecommerce.backend.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    
    @PostMapping
    public ResponseEntity<CartResponse> addToCart (@Valid @RequestBody CartItemRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(cartService.addToCart(email,request));
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(cartService.getCart(email));
    }

    @PutMapping
    public ResponseEntity<CartResponse> updateCart(@Valid @RequestBody CartItemRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(cartService.updateCartItem(email, request));
    }
	     
    @DeleteMapping("/{productId}")
    public ResponseEntity<String> removeFromCart (@PathVariable Long productId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.removeFromCart(email, productId);
        return ResponseEntity.ok("Product removed from cart successfully");
    }
    
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.clearCart(email);
        return ResponseEntity.ok("Cart cleared successfully");
    }
    
}
