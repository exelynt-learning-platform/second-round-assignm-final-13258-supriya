package com.ecommerce.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.ecommerce.backend.dto.request.PaymentRequest;
import com.ecommerce.backend.dto.response.PaymentResponse;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.exception.UnauthorizedException;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.serviceImpl.PaymentServiceImpl;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(1L).email("test@test.com").build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("test@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void processPayment_unsupportedMethod_throwsBadRequest() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

        PaymentRequest request = PaymentRequest.builder().orderId(1L).paymentMethod("PAYPAL").build();

        assertThrows(BadRequestException.class, () -> paymentService.processPayment(request));
    }

    @Test
    void processPayment_orderNotFound_throwsException() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(orderRepository.findById(any())).thenReturn(Optional.empty());

        PaymentRequest request = PaymentRequest.builder().orderId(99L).paymentMethod("STRIPE").build();

        assertThrows(ResourceNotFoundException.class, () -> paymentService.processPayment(request));
    }

    @Test
    void processPayment_unauthorizedUser_throwsException() {
        User otherUser = User.builder().id(2L).email("other@test.com").build();
        Order order = Order.builder().id(1L).user(otherUser).status(OrderStatus.CREATED).totalAmount(100.0).build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        PaymentRequest request = PaymentRequest.builder().orderId(1L).paymentMethod("STRIPE").build();

        assertThrows(UnauthorizedException.class, () -> paymentService.processPayment(request));
    }

    @Test
    void processPayment_alreadyPaid_throwsBadRequest() {
        Order order = Order.builder().id(1L).user(mockUser).status(OrderStatus.PAID).totalAmount(100.0).build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        PaymentRequest request = PaymentRequest.builder().orderId(1L).paymentMethod("STRIPE").build();

        assertThrows(BadRequestException.class, () -> paymentService.processPayment(request));
    }

    @Test
    void processPayment_stripeFailure_returnsFailed() {
        Order order = Order.builder().id(1L).user(mockUser).status(OrderStatus.CREATED).totalAmount(100.0).build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Stripe.apiKey is not set properly in test, so Stripe call will fail
        PaymentRequest request = PaymentRequest.builder().orderId(1L).paymentMethod("STRIPE").build();

        PaymentResponse response = paymentService.processPayment(request);

        assertNotNull(response);
        assertEquals("FAILED", response.getStatus());
        assertEquals("FAILED", response.getPaymentStatus());
    }

    @Test
    void handlePaymentSuccess_success() {
        Order order = Order.builder().id(1L).user(mockUser).status(OrderStatus.PENDING)
                .stripeSessionId("session_123").totalAmount(100.0).build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(orderRepository.findByStripeSessionId("session_123")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        PaymentResponse response = paymentService.handlePaymentSuccess("session_123");

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("PAID", response.getPaymentStatus());
        assertEquals(OrderStatus.PAID, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void handlePaymentSuccess_unauthorizedUser_throwsException() {
        User otherUser = User.builder().id(2L).email("other@test.com").build();
        Order order = Order.builder().id(1L).user(otherUser).status(OrderStatus.PENDING)
                .stripeSessionId("session_123").totalAmount(100.0).build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(orderRepository.findByStripeSessionId("session_123")).thenReturn(Optional.of(order));

        assertThrows(UnauthorizedException.class, () -> paymentService.handlePaymentSuccess("session_123"));
    }

    @Test
    void handlePaymentSuccess_sessionNotFound_throwsException() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(orderRepository.findByStripeSessionId(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.handlePaymentSuccess("invalid_session"));
    }
}
