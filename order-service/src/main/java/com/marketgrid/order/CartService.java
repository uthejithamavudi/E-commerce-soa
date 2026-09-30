package com.marketgrid.order;

import com.marketgrid.order.dto.AddToCartRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    public Cart getCart(String customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> cartRepository.save(Cart.builder()
                        .customerId(customerId)
                        .items(new ArrayList<>())
                        .build()));
    }

    public Cart addItemToCart(String customerId, AddToCartRequest request) {
        Cart cart = getCart(customerId);

        boolean found = false;
        for (CartItem item : cart.getItems()) {
            if (item.getProductId().equals(request.getProductId())) {
                item.setQuantity(item.getQuantity() + request.getQuantity());
                found = true;
                break;
            }
        }

        if (!found) {
            CartItem item = CartItem.builder()
                    .productId(request.getProductId())
                    .productName(request.getProductName())
                    .productImage(request.getProductImage())
                    .vendorId(request.getVendorId())
                    .vendorName(request.getVendorName() != null ? request.getVendorName() : "Vendor-" + request.getVendorId())
                    .price(request.getPrice())
                    .quantity(request.getQuantity())
                    .variantId(request.getVariantId())
                    .build();
            cart.getItems().add(item);
        }

        return cartRepository.save(cart);
    }

    public Cart removeItemFromCart(String customerId, String productId) {
        Cart cart = getCart(customerId);
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        return cartRepository.save(cart);
    }

    public void clearCart(String customerId) {
        Cart cart = getCart(customerId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
