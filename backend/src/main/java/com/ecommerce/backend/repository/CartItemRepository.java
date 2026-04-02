package com.ecommerce.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.backend.entity.Cart;
import com.ecommerce.backend.entity.CartItem;
import com.ecommerce.backend.entity.Product;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    void deleteAllByCart(Cart cart);

}
