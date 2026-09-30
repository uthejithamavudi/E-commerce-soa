package com.marketgrid.apigateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthGateController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        var services = discoveryClient.getServices();
        if (services.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("status", "INITIALIZING", "message", "Waiting for Eureka cache sync..."));
        }
        return ResponseEntity.ok(Map.of("status", "UP", "services_registered", services.size()));
    }
}
