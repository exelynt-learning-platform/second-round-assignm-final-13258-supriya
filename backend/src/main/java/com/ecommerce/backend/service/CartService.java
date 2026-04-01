package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.request.CartItemRequest;
import com.ecommerce.backend.dto.response.CartResponse;

public interface CartService {
    
    //Add, Remove, View Cart operations
    CartResponse addToCart (String email, CartItemRequest request);
    CartResponse getCart (String email);
    void removeFromCart (String email, Long productId);
    void clearCart (String email);
    CartResponse updateCartItem (String email, CartItemRequest request);
    
}
