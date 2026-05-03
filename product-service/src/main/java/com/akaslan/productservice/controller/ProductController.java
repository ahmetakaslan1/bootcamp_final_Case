package com.akaslan.productservice.controller;

import com.akaslan.common.dto.ApiResponse;
import com.akaslan.productservice.dto.ProductRequest;
import com.akaslan.productservice.dto.ProductResponse;
import com.akaslan.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Ürün Yönetimi", description = "Vitrindeki ürünleri listeleme, arama ve admin işlemleri")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Tüm Ürünleri Getir", description = "Vitrindeki tüm ürünleri sayfalayarak (pagination) getirir")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAll(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(productService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ürün Detayı", description = "Verilen ID'ye sahip ürünün detaylarını (ve stok durumunu) getirir")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.findById(id)));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Kategoriye Göre Ürünler", description = "Belirli bir kategoriye ait tüm ürünleri listeler")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(ApiResponse.success(productService.findByCategory(category)));
    }

    @GetMapping("/search")
    @Operation(summary = "Ürün Ara", description = "İsmine göre ürün araması yapar")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> search(@RequestParam String name) {
        return ResponseEntity.ok(ApiResponse.success(productService.search(name)));
    }

    @PostMapping
    @Operation(summary = "Yeni Ürün Ekle", description = "(Sadece Admin) Sisteme yeni bir ürün ekler")
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Ürün oluşturuldu", productService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Ürün Güncelle", description = "(Sadece Admin) Var olan bir ürünü günceller")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Ürün güncellendi", productService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Ürün Sil", description = "(Sadece Admin) Verilen ID'ye sahip ürünü sistemden siler")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Ürün silindi"));
    }
}
