package com.marketgrid.order;

import com.marketgrid.order.dto.AddToCartRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<Cart> getCart(Authentication authentication) {
        String customerId = authentication.getCredentials().toString();
        return ResponseEntity.ok(cartService.getCart(customerId));
    }

    @PostMapping("/items")
    public ResponseEntity<Cart> addItemToCart(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequest request) {
        String customerId = authentication.getCredentials().toString();
        return ResponseEntity.ok(cartService.addItemToCart(customerId, request));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Cart> removeItemFromCart(
            Authentication authentication,
            @PathVariable String productId) {
        String customerId = authentication.getCredentials().toString();
        return ResponseEntity.ok(cartService.removeItemFromCart(customerId, productId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearCart(Authentication authentication) {
        String customerId = authentication.getCredentials().toString();
        cartService.clearCart(customerId);
        return ResponseEntity.ok(Map.of("message", "Cart cleared successfully"));
    }
}
