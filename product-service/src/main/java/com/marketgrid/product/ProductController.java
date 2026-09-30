package com.marketgrid.product;

import com.marketgrid.product.dto.ProductRequest;
import com.marketgrid.product.dto.StockReservationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<Product>> getPublicProducts(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(productService.getPublicProducts(categoryId, keyword, page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/vendor")
    public ResponseEntity<Page<Product>> getVendorProducts(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String vendorId = authentication.getCredentials().toString();
        return ResponseEntity.ok(productService.getVendorProducts(vendorId, page, size));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {
        String vendorId = authentication.getCredentials().toString();
        String vendorEmail = authentication.getName();
        return ResponseEntity.ok(productService.createProduct(request, vendorId, vendorEmail));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {
        String vendorId = authentication.getCredentials().toString();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        return ResponseEntity.ok(productService.updateProduct(id, request, vendorId, role));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(
            @PathVariable String id,
            Authentication authentication) {
        String vendorId = authentication.getCredentials().toString();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        productService.deleteProduct(id, vendorId, role);
        return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
    }

    @PostMapping("/reserve-stock")
    public ResponseEntity<Map<String, Boolean>> reserveStock(@RequestBody List<StockReservationRequest> items) {
        boolean success = productService.reserveStock(items);
        return ResponseEntity.ok(Map.of("success", success));
    }

    @PostMapping("/release-stock")
    public ResponseEntity<Map<String, String>> releaseStock(@RequestBody List<StockReservationRequest> items) {
        productService.releaseStock(items);
        return ResponseEntity.ok(Map.of("message", "Stock released"));
    }

    @PostMapping("/deduct-stock")
    public ResponseEntity<Map<String, String>> deductStock(@RequestBody List<StockReservationRequest> items) {
        productService.deductStock(items);
        return ResponseEntity.ok(Map.of("message", "Stock deducted"));
    }
}
