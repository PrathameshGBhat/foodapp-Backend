package com.cts.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cts.dtos.NotificationDto;
import com.cts.dtos.OrderItemDto;
import com.cts.dtos.OrdersDto;
import com.cts.entities.OrdersRequest;
import com.cts.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    @Autowired
    private OrderService orderService;
  
 
    
    // ==================== QUERY ENDPOINTS ====================
    
    /**
     * GET /api/orders
     * Retrieve all orders
     */
    @PreAuthorize("hasRole('VENDOR')")
    @GetMapping
    public ResponseEntity<List<OrdersDto>> getAllOrders() {
        logger.info("📋 Fetching all orders");
        List<OrdersDto> list = orderService.getAllOrders();
        logger.info("✅ Retrieved {} orders", list.size());
        return ResponseEntity.ok(list);
    }
    
    /**
     * GET /api/orders/{orderId}
     * Retrieve order by ID
     */
    @PreAuthorize("hasAnyRole('VENDOR','CUSTOMER','ADMIN')")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrdersDto> getOrderById(@PathVariable("orderId") Long orderId) {
        logger.info("🔍 Fetching order with ID: {}", orderId);
        OrdersDto dto = orderService.getOrdersById(orderId);
        if (dto == null) {
            logger.warn("❌ Order not found with ID: {}", orderId);
            return ResponseEntity.notFound().build();
        }
        logger.info("✅ Order found - Status: {}", dto.getOrderStatus());
        return ResponseEntity.ok(dto);
    }
    
    // ==================== CREATE ENDPOINT ====================
    
    /**
     * POST /api/orders
     * Create new order
     * Initial status: PLACED
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ResponseEntity<OrdersDto> createOrders(@RequestBody OrdersRequest request) {
        logger.info("Creating new order");
        OrdersDto created = orderService.addOrders(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    
    // ==================== SAGA ORCHESTRATION ENDPOINT ====================
    
    /**
     * ╔═════════════════════════════════════════════════════════╗
     * ║     SAGA CALLBACK: Payment Service → Order Service      ║
     * ╚═════════════════════════════════════════════════════════╝
     * 
     * PUT /api/orders/{orderId}/update-status?paymentStatus={status}
     * 
     * Called by Payment Service after payment processing
     * Maps payment status to order status:
     * 
     * | Payment Status | Order Status | Reason |
     * |---|---|---|
     * | SUCCESS | CONFIRMED | Payment successful, order confirmed |
     * | CANCELLED | CANCELLED | Payment cancelled or refunded |
     * | REFUNDED | CANCELLED | Wallet refunded, order cancelled |
     * | FAILED | CANCELLED | Payment failed, order remains PLACED |
     * 
     * @param orderId Order ID from Payment Service
     * @param paymentStatus Payment status (SUCCESS, CANCELLED, REFUNDED, FAILED)
     * @return Updated OrdersDto with new status
     * 
     * Example Response:
     * {
     *   "orderId": 23,
     *   "orderStatus": "CONFIRMED",
     *   "customerId": 334,
     *   "subTotal": 116.0,
     *   "updatedAt": "2025-11-19T13:50:00.000000"
     * }
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/{orderId}/update-status")
    public ResponseEntity<?> updateOrderStatusByPayment(
            @PathVariable("orderId") Long orderId,
            @RequestParam String paymentStatus) {
        
        logger.info("🔔 ╔════════════════════════════════════════╗");
        logger.info("🔔 ║  SAGA CALLBACK from Payment Service   ║");
        logger.info("🔔 ║  Order ID: {}", orderId);
        logger.info("🔔 ║  Payment Status: {}", paymentStatus);
        logger.info("🔔 ╚════════════════════════════════════════╝");
        
        try {
            // Validate payment status
            if (paymentStatus == null || paymentStatus.trim().isEmpty()) {
                logger.error("❌ Payment status is null or empty");
                return ResponseEntity.badRequest().body(
                    createErrorResponse("VALIDATION_ERROR", "Payment status cannot be null or empty")
                );
            }
            
            String upperPaymentStatus = paymentStatus.toUpperCase();
            
            // Validate allowed payment statuses
            if (!isValidPaymentStatus(upperPaymentStatus)) {
                logger.error("❌ Invalid payment status: {}", paymentStatus);
                return ResponseEntity.badRequest().body(
                    createErrorResponse("INVALID_STATUS", 
                        "Payment status must be one of: SUCCESS, CANCELLED, REFUNDED, FAILED")
                );
            }
            
            logger.info("📝 Updating order {} status based on payment status: {}", orderId, upperPaymentStatus);
            
            // Map payment status to order status and update
            OrdersDto updated = orderService.updateOrderStatusByPayment(orderId, upperPaymentStatus);
            
            logger.info("✅ ╔════════════════════════════════════════╗");
            logger.info("✅ ║  Order Status Updated Successfully     ║");
            logger.info("✅ ║  Order ID: {}", updated.getOrderId());
            logger.info("✅ ║  New Status: {}", updated.getOrderStatus());
            logger.info("✅ ╚════════════════════════════════════════╝");
            
            return ResponseEntity.ok(updated);
            
        } catch (IllegalArgumentException ex) {
            logger.error("❌ Order not found with ID: {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                createErrorResponse("NOT_FOUND", "Order not found with ID: " + orderId)
            );
            
        } catch (Exception ex) {
            logger.error("❌ Error updating order status: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                createErrorResponse("INTERNAL_ERROR", "Error updating order status: " + ex.getMessage())
            );
        }
    }

    
    // ==================== LEGACY/MANUAL ENDPOINT ====================
    
    /**
     * PUT /api/orders/{orderId}
     * 
     * Legacy endpoint for manual status update
     * Updates order status from PLACED → CONFIRMED manually
     * 
     * @deprecated Use /update-status endpoint instead for Payment Service integration
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping(value = "/{orderId}")
    public ResponseEntity<?> updateOrderStatusManual(@PathVariable("orderId") Long orderId) {
        logger.info("🔄 Manual order status update requested for order: {}", orderId);
        
        try {
            logger.info("📝 Updating order {} status", orderId);
            orderService.updateOrderStatus(orderId);
            
            OrdersDto updated = orderService.getOrdersById(orderId);
            logger.info("✅ Order {} status updated to: {}", orderId, updated.getOrderStatus());
            return ResponseEntity.ok(updated);
            
        } catch (IllegalArgumentException ex) {
            logger.error("❌ Order not found with ID: {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                createErrorResponse("NOT_FOUND", "Order not found with ID: " + orderId)
            );
            
        } catch (RuntimeException ex) {
            logger.error("❌ Runtime error while updating order: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                createErrorResponse("INTERNAL_ERROR", ex.getMessage())
            );
        }
    }

    // ==================== DELETE ENDPOINT ====================
    
    /**
     * DELETE /api/orders/{orderId}
     * Delete order by ID
     */
//    @PreAuthorize("permitAll()")
    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long orderId) {
        logger.info("🗑️ Delete request for order: {}", orderId);
        
        try {
            orderService.deleteOrder(orderId);
            logger.info("✅ Order {} deleted successfully", orderId);
            
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                createSuccessResponse("Order deleted successfully", orderId)
            );
            
        } catch (IllegalArgumentException ex) {
            logger.error("❌ Order not found with ID: {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                createErrorResponse("NOT_FOUND", "Order not found with ID: " + orderId)
            );
            
        } catch (Exception ex) {
            logger.error("❌ Error deleting order: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                createErrorResponse("INTERNAL_ERROR", "Error deleting order: " + ex.getMessage())
            );
        }
    }

    // ==================== HELPER METHODS ====================
    
    /**
     * Validate if payment status is one of the allowed values
     */
    private boolean isValidPaymentStatus(String status) {
        return status.equals("SUCCESS") || 
               status.equals("CANCELLED") || 
               status.equals("REFUNDED") || 
               status.equals("FAILED");
    }
    
    /**
     * Create error response object
     */
    private java.util.Map<String, Object> createErrorResponse(String errorCode, String message) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("status", "ERROR");
        response.put("errorCode", errorCode);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    /**
     * Create success response object
     */
    private java.util.Map<String, Object> createSuccessResponse(String message, Long orderId) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", message);
        response.put("orderId", orderId);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}