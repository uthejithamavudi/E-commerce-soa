package com.marketgrid.vendor;

import com.marketgrid.vendor.dto.VendorOnboardRequest;
import com.marketgrid.vendor.dto.VendorStatusUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @PostMapping("/onboard")
    public ResponseEntity<Vendor> onboardVendor(
            @Valid @RequestBody VendorOnboardRequest request,
            Authentication authentication) {
        String userId = authentication.getCredentials().toString();
        String email = authentication.getName();
        return ResponseEntity.ok(vendorService.onboardVendor(request, userId, email));
    }

    @GetMapping("/profile")
    public ResponseEntity<Vendor> getMyProfile(Authentication authentication) {
        String userId = authentication.getCredentials().toString();
        return ResponseEntity.ok(vendorService.getProfileByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vendor> getVendorById(@PathVariable String id) {
        return ResponseEntity.ok(vendorService.getVendorById(id));
    }

    @GetMapping
    public ResponseEntity<Page<Vendor>> getVendors(
            @RequestParam(required = false) VendorStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(vendorService.getVendorsByStatus(status, page, size));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Vendor> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody VendorStatusUpdateRequest request) {
        return ResponseEntity.ok(vendorService.updateStatus(id, request));
    }
}
