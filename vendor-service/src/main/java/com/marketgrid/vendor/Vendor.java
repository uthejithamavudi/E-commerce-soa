package com.marketgrid.vendor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "vendors")
public class Vendor {
    @Id
    private String id;
    private String ownerUserId;
    private String ownerName;
    private String ownerEmail;

    private String storeName;
    private String slug;
    private String phone;
    private String businessType;

    private String gstin;
    private String panNumber;

    private String bankName;
    private String accountNumber;
    private String ifscCode;

    private VendorStatus status;

    private Double rating;
    private int totalProducts;
    private int totalOrders;

    private Instant joinedDate;
    private Instant updatedAt;
}
