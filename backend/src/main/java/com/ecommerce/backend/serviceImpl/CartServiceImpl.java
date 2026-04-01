package com.ecommerce.backend.serviceImpl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ecommerce.backend.dto.request.CartItemRequest;
import com.ecommerce.backend.dto.response.CartResponse;
import com.ecommerce.backend.entity.Cart;
import com.ecommerce.backend.entity.CartItem;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.CartItemRepository;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.service.CartService;
import com.ecommerce.backend.util.Constant;
import com.ecommerce.backend.util.MapperUtil;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public CartServiceImpl(CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }
    
    //Get, Add, Remove Cart operations will be implemented here

    //Get Cart by User ID
    @Override
    public CartResponse getCart (String email) {
        
        Cart cart = getOrCreateCart(email);
        return MapperUtil.toCartResponse(cart); 
    }
    
    //Add to Cart
    @Override
    public CartResponse addToCart(String email, CartItemRequest request) {
        
        //User user = getUser(userId);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(Constant.PRODUCT_NOT_FOUND));
                
    

    // Edge Case : Out of Stock
    if (product.getStock() == 0) {
        throw new BadRequestException(Constant.PRODUCT_OUT_OF_STOCK);
    }
    
    Cart cart = getOrCreateCart(email);

    Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndProduct(cart, product);

    // If product already in cart, update quantity
    if (existingCartItem.isPresent()) {
        CartItem cartItem = existingCartItem.get();
        int newQuantity = cartItem.getQuantity() + request.getQuantity();
    

    // Edge Case : Exceeding Stock
    if(newQuantity > product.getStock()) {
        throw new BadRequestException(Constant.QUANTITY_EXCEEDS_STOCK);
    }
    

    cartItem.setQuantity(newQuantity);
    cartItemRepository.save(cartItem);

 } else {
    // Edge Case : Quantity Exceeds Stock
    if(request.getQuantity() > product.getStock()) {
        throw new BadRequestException(Constant.QUANTITY_EXCEEDS_STOCK);
    }

    CartItem newItem = CartItem.builder()
            .cart(cart)
            .product(product)
            .quantity(request.getQuantity())
            .build();

            cartItemRepository.save(newItem);
 }

    //return MapperUtil.toCartResponse(cartRepository.findById(cart.getId()));


    return MapperUtil.toCartResponse(
    cartRepository.findById(cart.getId())
        .orElseThrow(() -> new ResourceNotFoundException(Constant.CART_ITEM_NOT_FOUND)));
    
        //return MapperUtil.toCartResponse(cart);
}

    // Update Cart Item
    @Override
    public CartResponse updateCartItem(String email, CartItemRequest request) {
        
        Cart cart = getOrCreateCart(email);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(Constant.PRODUCT_NOT_FOUND));

                CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new ResourceNotFoundException(Constant.CART_ITEM_NOT_FOUND));

                //Edge Case : Quantity Zero -> Remove Item
                if (request.getQuantity() == 0) {
                    cartItemRepository.delete(cartItem);
                } else {
                    if (request.getQuantity() > product.getStock()) {
                        throw new BadRequestException(Constant.QUANTITY_EXCEEDS_STOCK);
                    }

                    cartItem.setQuantity(request.getQuantity());
                    cartItemRepository.save(cartItem);
                }

                return MapperUtil.toCartResponse(cart);
            }

    // Remove Cart Item
    @Override
    public void removeFromCart(String email, Long productId) {
        
        Cart cart = getOrCreateCart(email);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(Constant.PRODUCT_NOT_FOUND));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new ResourceNotFoundException(Constant.CART_ITEM_NOT_FOUND));

        cartItemRepository.delete(cartItem);
    }

    // Clear Cart
    @Override
    public void clearCart(String email) {
        
        Cart cart = getOrCreateCart(email);
        
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    // Helper Methods
    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(Constant.USER_NOT_FOUND));
    }

   private Cart getOrCreateCart(String email) {
        User user = getUser(email);

        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                                       .user(user)
                                       .build();
                    return cartRepository.save(newCart);
                });
    }             
}

