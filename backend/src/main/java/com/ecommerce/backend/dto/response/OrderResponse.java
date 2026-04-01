package com.ecommerce.backend.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class OrderResponse {

    private Long orderId;
    private String shippingAddress;
    private Double totalPrice;
    private List<OrderItemResponse> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder

    public static class OrderItemResponse {

             private Long productId;
             private String productName;
             private Integer quantity;
             private Double price;
    }
}
