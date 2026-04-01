package com.ecommerce.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.backend.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {}
