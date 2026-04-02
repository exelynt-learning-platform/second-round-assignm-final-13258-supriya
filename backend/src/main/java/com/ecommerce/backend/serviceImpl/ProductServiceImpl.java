package com.ecommerce.backend.serviceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ecommerce.backend.dto.request.ProductRequest;
import com.ecommerce.backend.dto.response.ProductResponse;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.service.ProductService;
import com.ecommerce.backend.util.Constant;
import com.ecommerce.backend.util.MapperUtil;

@Service
public class ProductServiceImpl implements ProductService{

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    public ProductResponse createProduct(ProductRequest productRequest) {
        
        Product product = MapperUtil.toProduct(productRequest);

        return MapperUtil.toProductResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(Long productId, ProductRequest productRequest) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(Constant.PRODUCT_NOT_FOUND)); 

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription()); 
        product.setPrice(productRequest.getPrice());       
        product.setStock(productRequest.getStock());
        product.setImageUrl(productRequest.getImageUrl());
        
        return MapperUtil.toProductResponse(productRepository.save(product));
    }

    public void deleteProduct(Long productId) {
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(Constant.PRODUCT_NOT_FOUND));
        productRepository.delete(product);
    }

    public ProductResponse getProductById(Long productId) {
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(Constant.PRODUCT_NOT_FOUND));
        return MapperUtil.toProductResponse(product);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(MapperUtil::toProductResponse)
                .collect(Collectors.toList());
    }
}
