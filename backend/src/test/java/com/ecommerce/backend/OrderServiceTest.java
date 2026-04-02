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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ecommerce.backend.dto.request.OrderRequest;
import com.ecommerce.backend.dto.response.OrderResponse;
import com.ecommerce.backend.entity.Cart;
import com.ecommerce.backend.entity.CartItem;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderItem;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.exception.UnauthorizedException;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.CartItemRepository;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.serviceImpl.OrderServiceImpl;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(1L).email("test@test.com").build();

        // Set up SecurityContext so getCurrentUser() works
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("test@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createOrder_success() {
        Product product = Product.builder().id(1L).name("Product").price(100.0).stock(10).build();
        CartItem cartItem = CartItem.builder().product(product).quantity(2).build();
        Cart cart = Cart.builder().id(1L).user(mockUser).cartItems(new ArrayList<>(List.of(cartItem))).build();

        Order savedOrder = Order.builder()
                .id(1L).user(mockUser).totalAmount(200.0).status(OrderStatus.CREATED)
                .shippingAddress("Pune")
                .orderItems(List.of(OrderItem.builder().product(product).quantity(2).price(100.0).build()))
                .build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUser(mockUser)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = orderService.createOrder(new OrderRequest("Pune"));

        assertNotNull(response);
        assertEquals(200.0, response.getTotalPrice());
        assertEquals(8, product.getStock()); // stock deducted
        verify(cartItemRepository).deleteAllByCart(cart);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_emptyCart_throwsBadRequest() {
        Cart cart = new Cart();
        cart.setCartItems(new ArrayList<>());

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUser(mockUser)).thenReturn(Optional.of(cart));

        assertThrows(BadRequestException.class,
                () -> orderService.createOrder(new OrderRequest("Pune")));
    }

    @Test
    void createOrder_insufficientStock_throwsBadRequest() {
        Product product = Product.builder().id(1L).name("Product").price(100.0).stock(1).build();
        CartItem cartItem = CartItem.builder().product(product).quantity(5).build();
        Cart cart = Cart.builder().id(1L).user(mockUser).cartItems(new ArrayList<>(List.of(cartItem))).build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUser(mockUser)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class,
                () -> orderService.createOrder(new OrderRequest("Pune")));
    }

    @Test
    void getOrderById_success() {
        Order order = Order.builder()
                .id(1L).user(mockUser).totalAmount(200.0).shippingAddress("Pune")
                .orderItems(new ArrayList<>())
                .build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getOrderId());
    }

    @Test
    void getOrderById_unauthorized_throwsException() {
        User otherUser = User.builder().id(2L).email("other@test.com").build();
        Order order = Order.builder()
                .id(1L).user(otherUser).totalAmount(200.0)
                .orderItems(new ArrayList<>())
                .build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(UnauthorizedException.class, () -> orderService.getOrderById(1L));
    }

    @Test
    void getOrderById_notFound_throwsException() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(orderRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(99L));
    }

    @Test
    void getOrdersByUserId_success() {
        Order order = Order.builder()
                .id(1L).user(mockUser).totalAmount(200.0).shippingAddress("Pune")
                .orderItems(new ArrayList<>())
                .build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(orderRepository.findByUser(mockUser)).thenReturn(List.of(order));

        List<OrderResponse> responses = orderService.getOrdersByUserId();

        assertEquals(1, responses.size());
    }

    @Test
    void updateOrder_success() {
        Order order = Order.builder()
                .id(1L).user(mockUser).status(OrderStatus.CREATED)
                .shippingAddress("Old Address").totalAmount(200.0)
                .orderItems(new ArrayList<>())
                .build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse response = orderService.updateOrder(1L, new OrderRequest("New Address"));

        assertEquals("New Address", order.getShippingAddress());
    }

    @Test
    void updateOrder_paidOrder_throwsBadRequest() {
        Order order = Order.builder()
                .id(1L).user(mockUser).status(OrderStatus.PAID)
                .orderItems(new ArrayList<>())
                .build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class,
                () -> orderService.updateOrder(1L, new OrderRequest("New Address")));
    }
}
