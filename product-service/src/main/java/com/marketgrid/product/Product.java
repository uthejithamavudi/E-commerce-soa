package com.marketgrid.product;

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
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String name;
    private String slug;
    private String description;

    private String vendorId;
    private String vendorName;

    private String categoryId;
    private String categoryName;
    private String subcategory;

    private BigDecimal price;
    private BigDecimal originalPrice;
    private Double discountPercentage;

    private int stock;
    private int reservedStock;
    private String sku;

    private ProductStatus status;

    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    private Double rating;
    private Integer reviewCount;

    private Instant createdAt;
    private Instant updatedAt;

    public int getAvailableStock() {
        return Math.max(0, stock - reservedStock);
    }
}
