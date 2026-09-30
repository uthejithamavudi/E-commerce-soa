package com.marketgrid.order;

import com.marketgrid.order.dto.CheckoutRequest;
import com.marketgrid.order.dto.UpdateSubOrderStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(
            Authentication authentication,
            @Valid @RequestBody CheckoutRequest request) {
        String userId = authentication.getCredentials().toString();
        String email = authentication.getName();
        return ResponseEntity.ok(orderService.checkout(userId, email, "Customer", request));
    }

    @GetMapping("/customer")
    public ResponseEntity<Page<Order>> getCustomerOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String userId = authentication.getCredentials().toString();
        return ResponseEntity.ok(orderService.getCustomerOrders(userId, page, size));
    }

    @GetMapping("/vendor")
    @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Order>> getVendorOrders(Authentication authentication) {
        String vendorId = authentication.getCredentials().toString();
        return ResponseEntity.ok(orderService.getVendorOrders(vendorId));
    }

    @PatchMapping("/suborders/{subOrderId}/status")
    @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
    public ResponseEntity<Order> updateSubOrderStatus(
            @PathVariable String subOrderId,
            Authentication authentication,
            @Valid @RequestBody UpdateSubOrderStatusRequest request) {
        String vendorId = authentication.getCredentials().toString();
        return ResponseEntity.ok(orderService.updateSubOrderStatus(subOrderId, request, vendorId));
    }
}
