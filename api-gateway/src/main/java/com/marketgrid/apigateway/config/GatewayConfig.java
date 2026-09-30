package com.marketgrid.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;
import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;

@Configuration
public class GatewayConfig {

    @Bean
    public RouterFunction<ServerResponse> authRoute() {
        return route("auth-service")
                .route(path("/api/v1/auth", "/api/v1/auth/**"), http())
                .filter(lb("AUTH-SERVICE"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> productRoute() {
        return route("product-service-products")
                .route(path("/api/v1/products", "/api/v1/products/**"), http())
                .filter(lb("PRODUCT-SERVICE"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> categoryRoute() {
        return route("product-service-categories")
                .route(path("/api/v1/categories", "/api/v1/categories/**"), http())
                .filter(lb("PRODUCT-SERVICE"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> vendorRoute() {
        return route("vendor-service")
                .route(path("/api/v1/vendors", "/api/v1/vendors/**"), http())
                .filter(lb("VENDOR-SERVICE"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderRoute() {
        return route("order-service-orders")
                .route(path("/api/v1/orders", "/api/v1/orders/**"), http())
                .filter(lb("ORDER-SERVICE"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> cartRoute() {
        return route("order-service-cart")
                .route(path("/api/v1/cart", "/api/v1/cart/**"), http())
                .filter(lb("ORDER-SERVICE"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> notificationRoute() {
        return route("notification-service")
                .route(path("/api/v1/notifications", "/api/v1/notifications/**"), http())
                .filter(lb("NOTIFICATION-SERVICE"))
                .build();
    }
}
