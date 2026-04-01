package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.request.PaymentRequest;
import com.ecommerce.backend.dto.response.PaymentResponse;

public interface PaymentService { 

    // Create Stripe session for payment
    PaymentResponse processPayment(PaymentRequest request);

    // Handle success redirect
    PaymentResponse handlePaymentSuccess(String sessionId);
}
