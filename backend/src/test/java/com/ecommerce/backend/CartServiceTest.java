package com.ecommerce.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.ecommerce.backend.serviceImpl.CartServiceImpl;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private User mockUser() {
        return User.builder().id(1L).email("test@test.com").build();
    }

    private Product mockProduct(int stock) {
        return Product.builder().id(1L).name("Test Product").stock(stock).price(100.0).build();
    }

    private Cart mockCart(User user) {
        return Cart.builder().id(1L).user(user).cartItems(new ArrayList<>()).build();
    }

    @Test
    void addToCart_success() {
        User user = mockUser();
        Product product = mockProduct(10);
        Cart cart = mockCart(user);

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(productRepository.findById(any())).thenReturn(Optional.of(product));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartRepository.findById(any())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());

        CartItemRequest request = new CartItemRequest(1L, 2);
        CartResponse response = cartService.addToCart("test@test.com", request);

        assertNotNull(response);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addToCart_outOfStock_throwsBadRequest() {
        Product product = mockProduct(0);

        when(productRepository.findById(any())).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class,
                () -> cartService.addToCart("test@test.com", new CartItemRequest(1L, 2)));
    }

    @Test
    void addToCart_quantityExceedsStock_throwsBadRequest() {
        User user = mockUser();
        Product product = mockProduct(3);
        Cart cart = mockCart(user);

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(productRepository.findById(any())).thenReturn(Optional.of(product));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class,
                () -> cartService.addToCart("test@test.com", new CartItemRequest(1L, 5)));
    }

    @Test
    void addToCart_existingItem_updatesQuantity() {
        User user = mockUser();
        Product product = mockProduct(10);
        Cart cart = mockCart(user);
        CartItem existingItem = CartItem.builder().id(1L).cart(cart).product(product).quantity(2).build();

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(productRepository.findById(any())).thenReturn(Optional.of(product));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(existingItem));
        when(cartRepository.findById(any())).thenReturn(Optional.of(cart));

        CartResponse response = cartService.addToCart("test@test.com", new CartItemRequest(1L, 3));

        assertNotNull(response);
        assertEquals(5, existingItem.getQuantity());
        verify(cartItemRepository).save(existingItem);
    }

    @Test
    void getCart_success() {
        User user = mockUser();
        Cart cart = mockCart(user);

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        CartResponse response = cartService.getCart("test@test.com");

        assertNotNull(response);
        assertEquals(cart.getId(), response.getCartId());
    }

    @Test
    void removeFromCart_success() {
        User user = mockUser();
        Product product = mockProduct(10);
        Cart cart = mockCart(user);
        CartItem cartItem = CartItem.builder().id(1L).cart(cart).product(product).quantity(2).build();

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(cartItem));

        cartService.removeFromCart("test@test.com", 1L);

        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void removeFromCart_productNotFound_throwsNotFound() {
        User user = mockUser();

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(mockCart(user)));
        when(productRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.removeFromCart("test@test.com", 99L));
    }

    @Test
    void clearCart_success() {
        User user = mockUser();
        Cart cart = mockCart(user);
        Product product = mockProduct(10);
        cart.setCartItems(new ArrayList<>(List.of(
                CartItem.builder().id(1L).cart(cart).product(product).quantity(1).build()
        )));

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        cartService.clearCart("test@test.com");

        verify(cartItemRepository).deleteAllByCart(cart);
        verify(cartRepository).save(cart);
    }

    @Test
    void updateCartItem_quantityZero_removesItem() {
        User user = mockUser();
        Product product = mockProduct(10);
        Cart cart = mockCart(user);
        CartItem cartItem = CartItem.builder().id(1L).cart(cart).product(product).quantity(2).build();

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(cartItem));

        cartService.updateCartItem("test@test.com", new CartItemRequest(1L, 0));

        verify(cartItemRepository).delete(cartItem);
    }
}
