package com.ecommerce.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.backend.dto.request.ProductRequest;
import com.ecommerce.backend.dto.response.ProductResponse;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.serviceImpl.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductRequest mockRequest() {
        return ProductRequest.builder()
                .name("Test Product")
                .description("Test Description")
                .price(99.99)
                .stock(10)
                .imageUrl("http://image.com/test.jpg")
                .build();
    }

    private Product mockProduct() {
        return Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(99.99)
                .stock(10)
                .imageUrl("http://image.com/test.jpg")
                .build();
    }

    @Test
    void createProduct_success() {
        ProductRequest request = mockRequest();
        Product product = mockProduct();

        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals("Test Product", response.getName());
        assertEquals(99.99, response.getPrice());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void getProductById_success() {
        Product product = mockProduct();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Product", response.getName());
    }

    @Test
    void getProductById_notFound_throwsException() {
        when(productRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99L));
    }

    @Test
    void getAllProducts_success() {
        Product product = mockProduct();

        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductResponse> responses = productService.getAllProducts();

        assertEquals(1, responses.size());
        assertEquals("Test Product", responses.get(0).getName());
    }

    @Test
    void updateProduct_success() {
        Product product = mockProduct();
        ProductRequest request = ProductRequest.builder()
                .name("Updated Product")
                .description("Updated Description")
                .price(149.99)
                .stock(20)
                .imageUrl("http://image.com/updated.jpg")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.updateProduct(1L, request);

        assertNotNull(response);
        assertEquals("Updated Product", product.getName());
        assertEquals(149.99, product.getPrice());
        assertEquals("http://image.com/updated.jpg", product.getImageUrl());
        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_notFound_throwsException() {
        when(productRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(99L, mockRequest()));
    }

    @Test
    void deleteProduct_success() {
        Product product = mockProduct();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_notFound_throwsException() {
        when(productRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(99L));
    }
}
