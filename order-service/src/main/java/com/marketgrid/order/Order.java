package com.marketgrid.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    private String customerUserId;
    private String customerName;
    private String customerEmail;

    private String shippingAddress;
    private String paymentMethod;
    private String paymentStatus; // UNPAID, PAID

    private BigDecimal grandTotal;
    private BigDecimal deliveryFee;

    private OrderStatus globalStatus;

    @Builder.Default
    private List<MultiVendorSubOrder> subOrders = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;
}
