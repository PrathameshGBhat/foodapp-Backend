package com.cognizant.paymentservice.client;

import com.cognizant.paymentservice.dto.OrderResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign Client for Order Service Integration
 * Handles Saga communication between Payment and Order services
 */
@FeignClient(
    name = "order-service", 
    url = "http://localhost:8083",
    configuration = CompositeFeignConfig.class
    
)
public interface OrderClient {
    
    /**
     * SAGA STEP: Updates order status based on payment status
     * Maps: SUCCESS → CONFIRMED, FAILED → CANCELLED, REFUNDED → CANCELLED
     * 
     * ✅ NOW RETURNS OrderResponseDto (was void - FIXED!)
     * 
     * @param orderId Order ID (as String to match Request)
     * @param paymentStatus Payment status (SUCCESS, FAILED, REFUNDED)
     * @return OrderResponseDto with updated order details
     * @throws feign.FeignException if communication fails
     */
    @PutMapping("/api/orders/{orderId}/update-status")
    OrderResponseDto updateOrderStatusByPayment(
        @PathVariable("orderId") String orderId,
        @RequestParam("paymentStatus") String paymentStatus
    );
}