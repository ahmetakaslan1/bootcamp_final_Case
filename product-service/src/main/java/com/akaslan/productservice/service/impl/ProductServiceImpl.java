package com.akaslan.productservice.service.impl;

import com.akaslan.common.exception.ResourceNotFoundException;
import com.akaslan.productservice.dto.ProductRequest;
import com.akaslan.productservice.dto.ProductResponse;
import com.akaslan.productservice.entity.Product;
import com.akaslan.productservice.repository.ProductRepository;
import com.akaslan.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.akaslan.productservice.client.InventoryClient;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final InventoryClient inventoryClient;

    @Override
    public Page<ProductResponse> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductResponse::from);
    }

    @Override
    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        Integer stock = 0;
        boolean inStock = false;
        try {
            var response = inventoryClient.getStock(id);
            if (response != null && response.getData() != null) {
                stock = response.getData().quantity();
                inStock = response.getData().isInStock();
            }
        } catch (Exception e) {
            // Eğer inventory-service çökmüşse, ürün sayfasını patlatma. Stok yoka düşsün (Circuit Breaker mantığı)
            System.err.println("Inventory service çağrılamadı (stok 0 sayıldı): " + e.getMessage());
        }

        return ProductResponse.from(product, stock, inStock);
    }

    @Override
    public List<ProductResponse> findByCategory(String category) {
        return productRepository.findByCategory(category)
            .stream()
            .map(ProductResponse::from)
            .toList();
    }

    @Override
    public List<ProductResponse> search(String name) {
        return productRepository.findByNameContainingIgnoreCase(name)
            .stream()
            .map(ProductResponse::from)
            .toList();
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = Product.builder()
            .name(request.name())
            .description(request.description())
            .price(request.price())
            .category(request.category())
            .imageUrl(request.imageUrl())
            .build();

        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    @Transactional
    @CachePut(value = "products", key = "#id")
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(request.category());
        product.setImageUrl(request.imageUrl());

        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }
        productRepository.deleteById(id);
    }
}
