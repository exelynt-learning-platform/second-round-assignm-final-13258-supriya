package com.ecommerce.backend.service;

import java.util.List;

import com.ecommerce.backend.dto.request.OrderRequest;
import com.ecommerce.backend.dto.response.OrderResponse;

public interface OrderService {

    OrderResponse createOrder(OrderRequest orderRequest);
    OrderResponse getOrderById(Long orderId);
    List<OrderResponse> getOrdersByUserId();
    OrderResponse updateOrder(Long orderId, OrderRequest orderRequest);
}
