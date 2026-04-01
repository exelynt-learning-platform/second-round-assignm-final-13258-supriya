package com.ecommerce.backend.dto.response;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder


public class CartResponse {

    private Long cartId;
    private List<CartItemResponse> items;
    private double totalPrice;

    
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public static class CartItemResponse {

    private Long productId;
    private String productName;
    private Integer quantity;
    private Double price;
}
    
}
