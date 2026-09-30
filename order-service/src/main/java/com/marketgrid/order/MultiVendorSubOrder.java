package com.marketgrid.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiVendorSubOrder {
    private String subOrderId;
    private String vendorId;
    private String vendorName;
    private BigDecimal subtotal;
    private SubOrderStatus status;
    private String trackingNumber;
    private Instant estimatedDelivery;

    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}
