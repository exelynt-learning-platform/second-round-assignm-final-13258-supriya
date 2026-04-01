package com.ecommerce.backend.util;

import java.util.List;
import java.util.stream.Collectors;

import com.ecommerce.backend.dto.request.ProductRequest;
import com.ecommerce.backend.dto.response.CartResponse;
import com.ecommerce.backend.dto.response.OrderResponse;
import com.ecommerce.backend.dto.response.ProductResponse;
import com.ecommerce.backend.entity.Cart;
import com.ecommerce.backend.entity.CartItem;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderItem;
import com.ecommerce.backend.entity.Product;

public class MapperUtil { 
        
    //PRODUCT
    
        public static Product toProduct(ProductRequest request) {
        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .imageUrl(request.getImageUrl())
                .build();
    }
        
        public static ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .build();
    }
    
    //CART

        public static CartResponse toCartResponse(Cart cart) {
        List<CartResponse.CartItemResponse> items = cart.getCartItems()
                .stream()
                .map((CartItem item)  -> CartResponse.CartItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .price(item.getProduct().getPrice())
                        .build())
                .collect(Collectors.toList());

        double total = items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(items)
                .totalPrice(total)
                .build();
    }
    
    //ORDER
     
            public static OrderResponse toOrderResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getOrderItems()
                .stream()
                .map((OrderItem item) -> OrderResponse.OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getId())
                .shippingAddress(order.getShippingAddress())
                .totalPrice(order.getTotalAmount())
                .items(items)
                .build();
    }


    
    
}
