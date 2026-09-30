package com.marketgrid.order.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import com.marketgrid.product.dto.StockReservationRequest;
import java.util.List;

@FeignClient(name = "product-service")
public interface ProductClient {
    @PostMapping("/api/v1/products/reserve")
    boolean reserveStock(@RequestBody List<StockReservationRequest> requests);
    
    @PostMapping("/api/v1/products/deduct")
    void deductStock(@RequestBody List<StockReservationRequest> requests);
}
