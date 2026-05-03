package com.akaslan.productservice.service;

import com.akaslan.productservice.dto.ProductRequest;
import com.akaslan.productservice.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Page<ProductResponse> findAll(Pageable pageable);
    ProductResponse findById(Long id);
    List<ProductResponse> findByCategory(String category);
    List<ProductResponse> search(String name);
    ProductResponse create(ProductRequest request);
    ProductResponse update(Long id, ProductRequest request);
    void delete(Long id);
}
