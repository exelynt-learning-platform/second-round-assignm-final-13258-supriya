package com.ecommerce.backend.serviceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
import com.ecommerce.backend.service.PaymentService;
import com.ecommerce.backend.util.Constant;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${stripe.secretKey}")
    private String secretKey;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public PaymentServiceImpl(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    // Get current user from JWT token
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(Constant.USER_NOT_FOUND));
    }

    // Create Stripe session for payment
    @Override
    public PaymentResponse processPayment(PaymentRequest request) {

        User user = getCurrentUser();

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(Constant.ORDER_NOT_FOUND));

        // Security: user can pay only own order
        if (!order.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException(Constant.ORDER_ACCESS_DENIED);
        }

        // Prevent duplicate payment
        if (order.getStatus() == OrderStatus.PAID) {
            throw new BadRequestException(Constant.ORDER_ALREADY_PAID);
        }

        try {
            Stripe.apiKey = secretKey;

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:8080/api/payments/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("http://localhost:8080/api/payments/cancel")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("inr")
                                                    // Stripe expects amount in paisa
                                                    .setUnitAmount((long) (order.getTotalAmount() * 100))
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Order #" + order.getId())
                                                                    .build())
                                                    .build())
                                    .build())
                    .build();

            Session session = Session.create(params);

            // Save sessionId & update status to PENDING
            order.setStripeSessionId(session.getId());
            order.setStatus(OrderStatus.PENDING);
            orderRepository.save(order);

            return PaymentResponse.builder()
                    .status("PENDING")
                    .message("Stripe session created successfully")
                    .orderId(order.getId())
                    .paymentMethod("STRIPE")
                    .paymentStatus("PENDING")
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .build();
        } catch (Exception e) {

            return PaymentResponse.builder()
                    .status("FAILED")
                    .message("Failed to create Stripe session: " + e.getMessage())
                    .orderId(order.getId())
                    .paymentMethod("STRIPE")
                    .paymentStatus("FAILED")
                    .build();
        }
    }

    // Handle Success redirect
    @Override
    public PaymentResponse handlePaymentSuccess(String sessionId) {

        Order order = orderRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(Constant.ORDER_NOT_FOUND));

        // Update order status to PAID
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        return PaymentResponse.builder()
                .status("SUCCESS")
                .message("Payment successful")
                .orderId(order.getId())
                .paymentMethod("STRIPE")
                .paymentStatus("PAID")
                .build();
    }
}
