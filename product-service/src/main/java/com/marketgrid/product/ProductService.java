package com.marketgrid.product;

import com.marketgrid.product.dto.ProductRequest;
import com.marketgrid.product.dto.StockReservationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Page<Product> getPublicProducts(String categoryId, String keyword, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (keyword != null && !keyword.isBlank()) {
            return productRepository.findByNameContainingIgnoreCaseAndStatus(keyword.trim(), ProductStatus.ACTIVE, pageable);
        } else if (categoryId != null && !categoryId.isBlank()) {
            return productRepository.findByCategoryIdAndStatus(categoryId, ProductStatus.ACTIVE, pageable);
        } else {
            return productRepository.findByStatus(ProductStatus.ACTIVE, pageable);
        }
    }

    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public Page<Product> getVendorProducts(String vendorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findByVendorId(vendorId, pageable);
    }

    public Product createProduct(ProductRequest request, String vendorId, String vendorName) {
        String slug = request.getName().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "-") + "-" + System.currentTimeMillis();

        Product product = Product.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .vendorId(vendorId)
                .vendorName(vendorName != null ? vendorName : "Vendor-" + vendorId)
                .categoryId(request.getCategoryId())
                .categoryName(request.getCategoryName())
                .subcategory(request.getSubcategory())
                .price(request.getPrice())
                .originalPrice(request.getOriginalPrice() != null ? request.getOriginalPrice() : request.getPrice())
                .stock(request.getStock())
                .reservedStock(0)
                .sku(request.getSku() != null ? request.getSku() : "SKU-" + System.currentTimeMillis())
                .status(request.getStatus() != null ? request.getStatus() : ProductStatus.ACTIVE)
                .images(request.getImages() != null ? request.getImages() : List.of())
                .variants(request.getVariants() != null ? request.getVariants() : List.of())
                .rating(0.0)
                .reviewCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return productRepository.save(product);
    }

    public Product updateProduct(String id, ProductRequest request, String authenticatedVendorId, String authenticatedRole) {
        Product product = getProductById(id);

        if (!"ROLE_ADMIN".equals(authenticatedRole) && !product.getVendorId().equals(authenticatedVendorId)) {
            throw new RuntimeException("Unauthorized: You do not own this product");
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategoryId(request.getCategoryId());
        product.setCategoryName(request.getCategoryName());
        product.setSubcategory(request.getSubcategory());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setStock(request.getStock());
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }
        if (request.getImages() != null) {
            product.setImages(request.getImages());
        }
        if (request.getVariants() != null) {
            product.setVariants(request.getVariants());
        }
        product.setUpdatedAt(Instant.now());

        return productRepository.save(product);
    }

    public void deleteProduct(String id, String authenticatedVendorId, String authenticatedRole) {
        Product product = getProductById(id);
        if (!"ROLE_ADMIN".equals(authenticatedRole) && !product.getVendorId().equals(authenticatedVendorId)) {
            throw new RuntimeException("Unauthorized: You do not own this product");
        }
        productRepository.delete(product);
    }

    // Synchronized Atomic Inventory Logic
    public synchronized boolean reserveStock(List<StockReservationRequest> items) {
        log.info("Attempting stock reservation for {} items", items.size());
        for (StockReservationRequest item : items) {
            Product product = getProductById(item.getProductId());
            if (product.getAvailableStock() < item.getQuantity()) {
                log.warn("Insufficient stock for product: {}", product.getId());
                return false;
            }
        }

        for (StockReservationRequest item : items) {
            Product product = getProductById(item.getProductId());
            product.setReservedStock(product.getReservedStock() + item.getQuantity());
            productRepository.save(product);
        }
        return true;
    }

    public synchronized void releaseStock(List<StockReservationRequest> items) {
        for (StockReservationRequest item : items) {
            try {
                Product product = getProductById(item.getProductId());
                product.setReservedStock(Math.max(0, product.getReservedStock() - item.getQuantity()));
                productRepository.save(product);
            } catch (Exception e) {
                log.error("Failed to release stock for product: {}", item.getProductId(), e);
            }
        }
    }

    public synchronized void deductStock(List<StockReservationRequest> items) {
        for (StockReservationRequest item : items) {
            Product product = getProductById(item.getProductId());
            product.setStock(Math.max(0, product.getStock() - item.getQuantity()));
            product.setReservedStock(Math.max(0, product.getReservedStock() - item.getQuantity()));
            if (product.getStock() == 0) {
                product.setStatus(ProductStatus.OUT_OF_STOCK);
            }
            productRepository.save(product);
        }
    }
}
