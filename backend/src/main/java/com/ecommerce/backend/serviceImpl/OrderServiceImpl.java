package com.ecommerce.backend.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ecommerce.backend.dto.request.OrderRequest;
import com.ecommerce.backend.dto.response.OrderResponse;
import com.ecommerce.backend.entity.*;
import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.exception.UnauthorizedException;
import com.ecommerce.backend.repository.*;
import com.ecommerce.backend.service.OrderService;
import com.ecommerce.backend.util.Constant;
import com.ecommerce.backend.util.MapperUtil;

import jakarta.transaction.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CartRepository cartRepository,
                            CartItemRepository cartItemRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository) {

        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

        // GET CURRENT USER FROM JWT
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(Constant.USER_NOT_FOUND));
    }


    // CREATE ORDER
    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {

        User user = getCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException(Constant.CART_NOT_FOUND));

        // Edge: Empty cart
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new BadRequestException(Constant.CART_EMPTY);
        }

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setStatus(OrderStatus.CREATED);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = productRepository.findByIdForUpdate(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(Constant.PRODUCT_NOT_FOUND));

            int remainingStock = product.getStock() - cartItem.getQuantity();
            if (remainingStock < 0) {
                throw new BadRequestException(Constant.ORDER_STOCK_CHANGED);
            }

            product.setStock(remainingStock);
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice()); // snapshot
            orderItems.add(orderItem);
        }

        // Calculate total
        double totalAmount = orderItems.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        // Clear cart
        cartItemRepository.deleteAllByCart(cart);
        cart.getCartItems().clear();
        cartRepository.save(cart);

        return MapperUtil.toOrderResponse(savedOrder);
    }

    // GET ORDER BY ID
    @Override
    public OrderResponse getOrderById (Long orderId) {
        
        User user = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(Constant.ORDER_NOT_FOUND));

        // Unauthorized access
        if (!order.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException(Constant.UNAUTHORIZED);
        }

        return MapperUtil.toOrderResponse(order);
    }

    // GET USER ORDERS
    @Override
    public List<OrderResponse> getOrdersByUserId() {

        User user = getCurrentUser();

        return orderRepository.findByUser(user)
                .stream()
                .map(MapperUtil::toOrderResponse)
                .collect(Collectors.toList());
    }

    // UPDATE ORDER (only shipping address)
    @Override
    public OrderResponse updateOrder(Long orderId, OrderRequest orderRequest) {
        
        User user = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(Constant.ORDER_NOT_FOUND));

        // Unauthorized
        if (!order.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException(Constant.UNAUTHORIZED);
        }

        // Cannot update paid order
        if (order.getStatus() == OrderStatus.PAID) {
            throw new BadRequestException(Constant.ORDER_ALREADY_PAID);
        }

        order.setShippingAddress(orderRequest.getShippingAddress());

        return MapperUtil.toOrderResponse(orderRepository.save(order));
    }
}
