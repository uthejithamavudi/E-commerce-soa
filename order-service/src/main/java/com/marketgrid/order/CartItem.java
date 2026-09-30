package com.marketgrid.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private String productId;
    private String productName;
    private String productImage;
    private String vendorId;
    private String vendorName;
    private BigDecimal price;
    private int quantity;
    private String variantId;
}
