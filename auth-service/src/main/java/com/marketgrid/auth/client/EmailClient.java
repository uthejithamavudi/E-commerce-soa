package com.marketgrid.auth.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import com.marketgrid.notification.dto.EmailRequest;

@FeignClient(name = "notification-service")
public interface EmailClient {
    @PostMapping("/api/v1/notifications/send-email")
    void sendEmail(@RequestBody EmailRequest request);
}
