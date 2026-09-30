package com.marketgrid.order;

import com.marketgrid.order.client.EmailClient;
import com.marketgrid.order.dto.CheckoutRequest;
import com.marketgrid.order.dto.UpdateSubOrderStatusRequest;
import com.marketgrid.order.client.ProductClient;
import com.marketgrid.product.dto.StockReservationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductClient productClient;
    private final EmailClient emailClient;

    public Order checkout(String customerUserId, String customerEmail, String customerName, CheckoutRequest request) {
        Cart cart = cartService.getCart(customerUserId);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot checkout an empty cart");
        }

        // 1. Reserve Stock directly via ProductService
        List<StockReservationRequest> stockRequests = cart.getItems().stream()
                .map(i -> {
                    StockReservationRequest req = new StockReservationRequest();
                    req.setProductId(i.getProductId());
                    req.setQuantity(i.getQuantity());
                    return req;
                }).collect(Collectors.toList());

        boolean reserved = productClient.reserveStock(stockRequests);
        if (!reserved) {
            throw new RuntimeException("Insufficient stock available for checkout");
        }

        // 2. Multi-Vendor Splitting Engine
        Map<String, List<CartItem>> itemsByVendor = cart.getItems().stream()
                .collect(Collectors.groupingBy(CartItem::getVendorId));

        List<MultiVendorSubOrder> subOrders = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (Map.Entry<String, List<CartItem>> entry : itemsByVendor.entrySet()) {
            String vendorId = entry.getKey();
            List<CartItem> vendorItems = entry.getValue();

            String vendorName = vendorItems.get(0).getVendorName();
            BigDecimal subtotal = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();

            for (CartItem item : vendorItems) {
                BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                subtotal = subtotal.add(itemTotal);

                orderItems.add(OrderItem.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .productImage(item.getProductImage())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .variantId(item.getVariantId())
                        .build());
            }

            grandTotal = grandTotal.add(subtotal);

            MultiVendorSubOrder subOrder = MultiVendorSubOrder.builder()
                    .subOrderId("SUB-ORD-" + System.currentTimeMillis() + "-" + (vendorId.length() >= 4 ? vendorId.substring(vendorId.length() - 4) : vendorId))
                    .vendorId(vendorId)
                    .vendorName(vendorName)
                    .subtotal(subtotal)
                    .status(SubOrderStatus.CONFIRMED)
                    .trackingNumber("TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .estimatedDelivery(Instant.now().plusSeconds(86400 * 3))
                    .items(orderItems)
                    .build();

            subOrders.add(subOrder);
        }

        BigDecimal deliveryFee = BigDecimal.valueOf(50.0);
        grandTotal = grandTotal.add(deliveryFee);

        Order masterOrder = Order.builder()
                .customerUserId(customerUserId)
                .customerName(customerName != null ? customerName : "Customer")
                .customerEmail(customerEmail != null ? customerEmail : "customer@marketgrid.com")
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CARD")
                .paymentStatus("PAID")
                .grandTotal(grandTotal)
                .deliveryFee(deliveryFee)
                .globalStatus(OrderStatus.PROCESSING)
                .subOrders(subOrders)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        masterOrder = orderRepository.save(masterOrder);

        // 3. Deduct Stock directly
        productClient.deductStock(stockRequests);

        // 4. Clear Cart
        cartService.clearCart(customerUserId);

        // 5. Send Notification Email
        emailClient.sendEmail(masterOrder.getCustomerEmail(), "ORDER_CONFIRMATION", null, Map.of(
                "customerName", masterOrder.getCustomerName(),
                "orderId", masterOrder.getId(),
                "grandTotal", masterOrder.getGrandTotal().toString(),
                "subOrderCount", masterOrder.getSubOrders().size()
        ));

        return masterOrder;
    }

    public Page<Order> getCustomerOrders(String customerUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findByCustomerUserId(customerUserId, pageable);
    }

    public List<Order> getVendorOrders(String vendorId) {
        return orderRepository.findOrdersContainingVendor(vendorId);
    }

    public Order updateSubOrderStatus(String subOrderId, UpdateSubOrderStatusRequest request, String authenticatedVendorId) {
        List<Order> orders = orderRepository.findOrdersContainingVendor(authenticatedVendorId);

        for (Order order : orders) {
            for (MultiVendorSubOrder subOrder : order.getSubOrders()) {
                if (subOrder.getSubOrderId().equals(subOrderId) && subOrder.getVendorId().equals(authenticatedVendorId)) {
                    subOrder.setStatus(request.getStatus());
                    if (request.getTrackingNumber() != null) {
                        subOrder.setTrackingNumber(request.getTrackingNumber());
                    }
                    order.setUpdatedAt(Instant.now());

                    Order saved = orderRepository.save(order);

                    if (request.getStatus() == SubOrderStatus.SHIPPED || request.getStatus() == SubOrderStatus.DELIVERED) {
                        String emailType = request.getStatus() == SubOrderStatus.SHIPPED ? "ORDER_SHIPPED" : "ORDER_DELIVERED";
                        emailClient.sendEmail(order.getCustomerEmail(), emailType, null, Map.of(
                                "customerName", order.getCustomerName(),
                                "orderId", order.getId(),
                                "subOrderId", subOrderId,
                                "vendorName", subOrder.getVendorName(),
                                "trackingNumber", subOrder.getTrackingNumber() != null ? subOrder.getTrackingNumber() : "N/A"
                        ));
                    }
                    return saved;
                }
            }
        }

        throw new RuntimeException("Sub-order not found or unauthorized for vendor: " + authenticatedVendorId);
    }

    public Page<Order> getAllOrdersForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findAll(pageable);
    }
}
