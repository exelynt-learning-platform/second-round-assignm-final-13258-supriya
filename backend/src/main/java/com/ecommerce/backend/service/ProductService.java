package com.ecommerce.backend.service;

import java.util.List;

import com.ecommerce.backend.dto.request.ProductRequest;
import com.ecommerce.backend.dto.response.ProductResponse;

public interface ProductService { 
   
    //Create, Read, Update, Delete (CRUD) operations for Product
    ProductResponse createProduct(ProductRequest productRequest);
    
    List<ProductResponse> getAllProducts();

    ProductResponse getProductById(Long productId);


    ProductResponse updateProduct(Long productId, ProductRequest productRequest);

    void deleteProduct(Long productId);

    
}
