package com.ecommerce.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.backend.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {}
