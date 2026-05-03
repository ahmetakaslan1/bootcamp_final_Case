package com.akaslan.inventoryservice.service.impl;

import com.akaslan.inventoryservice.dto.InventoryRequest;
import com.akaslan.inventoryservice.dto.InventoryResponse;
import com.akaslan.inventoryservice.entity.Inventory;
import com.akaslan.inventoryservice.repository.InventoryRepository;
import com.akaslan.inventoryservice.client.ProductClient;
import com.akaslan.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductClient productClient;

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(Long productId) {
        log.info("Stok bilgisi sorgulanıyor, Product ID: {}", productId);

       // depoda ürün yoksa hata fırlatma, stok 0 olarak dön.
        // null  yememek için bu sektörde kullanılıyor muş ve bende eledim
        return inventoryRepository.findByProductId(productId)
                .map(inventory -> new InventoryResponse(
                        inventory.getProductId(),
                        inventory.getQuantity(),
                        inventory.getQuantity() > 0
                ))
                .orElseGet(() -> {
                    log.warn("Depoda kayıt bulunamadı! Product ID: {} için stok 0 kabul ediliyor.", productId);
                    return new InventoryResponse(productId, 0, false);
                });
    }

    @Override
    @Transactional
    public InventoryResponse updateInventory(InventoryRequest request) {
        log.info("Stok güncelleniyor. Product ID: {}, Miktar: {}", request.productId(), request.quantity());

        // ürün varmı 
        try {
            var response = productClient.getProductById(request.productId());
            if (response == null || response.getData() == null) {
                throw new IllegalArgumentException("Ürün bulunamadı! Olmayan ürüne stok eklenemez. Product ID: " + request.productId());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Ürün doğrulaması başarısız oldu! Product ID: " + request.productId(), e);
        }

        // MİMARİ DOKUNUŞ (Upsert): Eğer veritabanında varsa getir, yoksa YENİ nesne oluştur.
        // Bu sayede daha önce hiç eklenmemiş bir ürüne doğrudan stok girebiliriz.
        Inventory inventory = inventoryRepository.findByProductIdForUpdate(request.productId())
                .orElseGet(() -> Inventory.builder()
                        .productId(request.productId())
                        .quantity(0) // Başlangıçta 0, birazdan set edilecek
                        .build());

        inventory.setQuantity(inventory.getQuantity() + request.quantity());
        inventoryRepository.save(inventory);

        return new InventoryResponse(
                inventory.getProductId(),
                inventory.getQuantity(),
                inventory.getQuantity() > 0
        );
    }

    @Override
    @Transactional
    public void deductInventory(Long productId, Integer quantityToDeduct) {
        log.info("Stok düşülüyor. Product ID: {}, Düşülecek Miktar: {}", productId, quantityToDeduct);

        // Stok düşerken Pessimistic Lock (findByProductIdForUpdate) kullanıyoruz.
        Inventory inventory = inventoryRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new IllegalArgumentException("Stok düşülemedi! Depoda böyle bir ürün yok. Product ID: " + productId));

        if (inventory.getQuantity() < quantityToDeduct) {
            throw new IllegalArgumentException("Yetersiz stok! Mevcut: " + inventory.getQuantity() + ", İstenen: " + quantityToDeduct);
        }

        inventory.setQuantity(inventory.getQuantity() - quantityToDeduct);
        inventoryRepository.save(inventory);

        log.info("Stok başarıyla düşüldü. Product ID: {}, Kalan Stok: {}", productId, inventory.getQuantity());
    }

    @Override
    @Transactional
    public void restoreInventory(Long productId, Integer quantityToRestore) {
        log.info("Sipariş iptali: Stok geri yükleniyor. Product ID: {}, Miktar: {}", productId, quantityToRestore);

        inventoryRepository.findByProductIdForUpdate(productId).ifPresent(inventory -> {
            inventory.setQuantity(inventory.getQuantity() + quantityToRestore);
            inventoryRepository.save(inventory);
            log.info("Stok başarıyla iade edildi. Product ID: {}, Yeni Stok: {}", productId, inventory.getQuantity());
        });
    }
}