package com.ecommerce.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.User;

public interface OrderRepository extends JpaRepository <Order, Long>{

        List<Order> findByUser(User user);
        Optional<Order> findByStripeSessionId(String stripeSessionId);

}
