package com.marketgrid.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private final TemplateEngine templateEngine;

    public String buildTemplateContent(String type, Map<String, Object> params) {
        Context context = new Context();
        if (params != null) {
            params.forEach(context::setVariable);
        }

        String templateName = switch (type.toUpperCase()) {
            case "OTP", "OTP_VERIFICATION" -> "email/otp";
            case "EMAIL_VERIFICATION" -> "email/email-verification";
            case "PASSWORD_RESET" -> "email/password-reset";
            case "ORDER_CONFIRMATION" -> "email/order-confirmation";
            case "ORDER_SHIPPED" -> "email/order-shipped";
            case "ORDER_DELIVERED" -> "email/order-delivered";
            case "VENDOR_APPROVED" -> "email/vendor-approved";
            case "VENDOR_REJECTED" -> "email/vendor-rejected";
            case "VENDOR_SUSPENDED" -> "email/vendor-suspended";
            default -> "email/otp";
        };

        return templateEngine.process(templateName, context);
    }

    public String determineSubject(String type, String defaultSubject) {
        if (defaultSubject != null && !defaultSubject.isBlank()) {
            return defaultSubject;
        }

        return switch (type.toUpperCase()) {
            case "OTP", "OTP_VERIFICATION" -> "MarketGrid - Security Verification Code";
            case "EMAIL_VERIFICATION" -> "Welcome to MarketGrid - Verify Your Account";
            case "PASSWORD_RESET" -> "MarketGrid - Password Reset Code";
            case "ORDER_CONFIRMATION" -> "MarketGrid - Order Confirmation";
            case "ORDER_SHIPPED" -> "MarketGrid - Your Sub-Order Has Shipped";
            case "ORDER_DELIVERED" -> "MarketGrid - Package Delivered";
            case "VENDOR_APPROVED" -> "MarketGrid - Merchant Account Approved!";
            case "VENDOR_REJECTED" -> "MarketGrid - Merchant Application Update";
            case "VENDOR_SUSPENDED" -> "MarketGrid - Merchant Account Suspended";
            default -> "MarketGrid Notification";
        };
    }
}
