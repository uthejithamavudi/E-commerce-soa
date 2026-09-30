package com.marketgrid.vendor;

import com.marketgrid.vendor.client.EmailClient;
import com.marketgrid.vendor.dto.VendorOnboardRequest;
import com.marketgrid.vendor.dto.VendorStatusUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final EmailClient emailClient;

    public Vendor onboardVendor(VendorOnboardRequest request, String ownerUserId, String ownerEmail) {
        if (vendorRepository.existsByOwnerUserId(ownerUserId)) {
            throw new RuntimeException("User already has an onboarded vendor profile");
        }

        String slug = request.getStoreName().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "-") + "-" + System.currentTimeMillis();

        Vendor vendor = Vendor.builder()
                .ownerUserId(ownerUserId)
                .ownerEmail(ownerEmail != null ? ownerEmail : "vendor@marketgrid.com")
                .ownerName(request.getStoreName() + " Owner")
                .storeName(request.getStoreName())
                .slug(slug)
                .phone(request.getPhone())
                .businessType(request.getBusinessType())
                .gstin(request.getGstin())
                .panNumber(request.getPanNumber())
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .ifscCode(request.getIfscCode())
                .status(VendorStatus.PENDING)
                .rating(0.0)
                .totalProducts(0)
                .totalOrders(0)
                .joinedDate(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return vendorRepository.save(vendor);
    }

    public Vendor getProfileByUserId(String ownerUserId) {
        return vendorRepository.findByOwnerUserId(ownerUserId)
                .orElseThrow(() -> new RuntimeException("Vendor profile not found for user: " + ownerUserId));
    }

    public Vendor getVendorById(String id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + id));
    }

    public Page<Vendor> getVendorsByStatus(VendorStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (status != null) {
            return vendorRepository.findByStatus(status, pageable);
        }
        return vendorRepository.findAll(pageable);
    }

    public Vendor updateStatus(String vendorId, VendorStatusUpdateRequest request) {
        Vendor vendor = getVendorById(vendorId);
        vendor.setStatus(request.getStatus());
        vendor.setUpdatedAt(Instant.now());

        vendor = vendorRepository.save(vendor);

        String emailType = switch (request.getStatus()) {
            case ACTIVE -> "VENDOR_APPROVED";
            case REJECTED -> "VENDOR_REJECTED";
            case SUSPENDED -> "VENDOR_SUSPENDED";
            default -> "VENDOR_STATUS_UPDATE";
        };

        emailClient.sendEmail(vendor.getOwnerEmail(), emailType, null, Map.of(
                "storeName", vendor.getStoreName(),
                "status", vendor.getStatus().name(),
                "reason", request.getReason() != null ? request.getReason() : "N/A"
        ));

        return vendor;
    }
}
