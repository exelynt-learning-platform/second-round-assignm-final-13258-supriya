package com.ecommerce.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private String status;  //SUCCESS, FAILED, PENDING
    private String message;
    private Long orderId;
    private String paymentMethod;
    private String paymentStatus;
    private String sessionId;
    private String sessionUrl;

}
