package com.marketgrid.order.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name = "notification-service")
public interface EmailClient {
    @PostMapping("/api/v1/notification/send")
    void sendEmail(@RequestParam("to") String to, @RequestParam("type") String type, @RequestParam(value = "subject", required = false) String subject, @RequestBody Map<String, Object> variables);
}
