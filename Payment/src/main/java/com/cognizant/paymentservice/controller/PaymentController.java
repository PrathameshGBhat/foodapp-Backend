package com.cognizant.paymentservice.controller;

import com.cognizant.paymentservice.exception.ResourceNotFoundException;
import com.cognizant.paymentservice.exception.SagaExecutionException;
import com.cognizant.paymentservice.model.PaymentRequest;
import com.cognizant.paymentservice.model.PaymentResponse;
import com.cognizant.paymentservice.model.Transaction;
import com.cognizant.paymentservice.model.Wallet;
import com.cognizant.paymentservice.model.WalletTopUpRequest;
import com.cognizant.paymentservice.model.WalletTopUpResponse;
import com.cognizant.paymentservice.service.PaymentService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payment")
public class PaymentController {
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;
//    @PreAuthorize("hasAnyRole('CUSTOMER','VENDOR','ADMIN')")
    @PostMapping("/wallet/add")
	public WalletTopUpResponse addMoneyToWallet(@Valid @RequestBody WalletTopUpRequest request) {
		return paymentService.addMoneyToWallet(request);
	}
//    @PreAuthorize("hasAnyRole('CUSTOMER','VENDOR','ADMIN')")
	@GetMapping("/wallet/{userId}")
	public Wallet getWallet(@PathVariable String userId) {
		return paymentService.getWallet(userId);
	}

    // ==================== PAYMENT PROCESSING ====================

    /**
     * Process a payment
     * 
     * POST /payment/pay
     * Body: PaymentRequest
     * 
     * Response:
     * - 200 OK: Payment successful, Order Service confirmed
     * - 202 ACCEPTED: Payment successful but Order Service failed (Compensating TX executed, wallet refunded)
     * - 400 BAD_REQUEST: Validation or payment processing failed
     * - 503 SERVICE_UNAVAILABLE: Order Service unavailable
     */
   // @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/pay")
    public ResponseEntity<?> processPayment(@RequestBody PaymentRequest request) {
        logger.info("🔄 Received payment request for order: {}", 
            request.getRestaurant() != null ? request.getRestaurant().getOrderId() : "UNKNOWN");
        
        try {
            PaymentResponse response = paymentService.processPayment(request);
            logger.info("✅ Payment processed successfully - Saga completed");
            return ResponseEntity.ok(response);

        } catch (SagaExecutionException e) {
            // ✅ SAGA EXECUTED COMPENSATING TRANSACTION
            // Payment was processed but Order Service communication failed
            // Wallet has been refunded, transaction marked as COMPENSATED
            logger.warn("⚠️ SAGA COMPENSATION EXECUTED: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "COMPENSATED");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("errorCode", e.getErrorCode());
            errorResponse.put("details", "Payment processed successfully but Order Service communication failed. " +
                    "Compensating transaction executed - Wallet refunded if applicable.");
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(errorResponse);

        } catch (IllegalArgumentException e) {
            // Validation failed at request level
            logger.error("❌ Validation error: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "FAILED");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("errorCode", "VALIDATION_ERROR");
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            // Unexpected error
            logger.error("❌ Unexpected error during payment processing: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "FAILED");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("errorCode", "INTERNAL_SERVER_ERROR");
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Refund a payment
     * 
     * POST /payment/refund/{id}
     * 
     * Response:
     * - 200 OK: Refund successful
     * - 202 ACCEPTED: Refund processed but Order Service notification failed
     * - 404 NOT_FOUND: Transaction not found
     * - 400 BAD_REQUEST: Refund validation failed
     */
//	@PreAuthorize("hasAnyRole('CUSTOMER','VENDOR','ADMIN')")
    @PostMapping("/refund/{id}")
    public ResponseEntity<?> refundPayment(@PathVariable UUID id) {
        logger.info("🔄 Refund requested for transaction: {}", id);
        
        try {
            PaymentResponse response = paymentService.refundPayment(id);
            logger.info("✅ Refund processed successfully");
            return ResponseEntity.ok(response);

        } catch (SagaExecutionException e) {
            logger.warn("⚠️ Refund saga partially completed: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "PARTIALLY_COMPLETED");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("errorCode", e.getErrorCode());
            errorResponse.put("details", "Refund processed but Order Service notification may have failed");
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(errorResponse);

        } catch (Exception e) {
            logger.error("❌ Refund failed: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "FAILED");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("errorCode", "REFUND_ERROR");
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== RECOVERY ENDPOINT ====================

    /**
     * ╔═════════════════════════════════════════════════════════╗
     * ║   RECOVER COMPENSATED TRANSACTION - Finalize Saga      ║
     * ╚═════════════════════════════════════════════════════════╝
     * 
     * Call this endpoint after Order Service restarts to finalize 
     * the compensation for a failed payment saga.
     * 
     * Flow:
     * 1. Find the compensated transaction for the order
     * 2. Notify Order Service to CANCEL the order
     * 3. Order status updates from PLACED → CANCELLED
     * 
     * POST /payment/recover/{orderId}
     * 
     * Response:
     * - 200 OK: Order successfully updated to CANCELLED
     * - 404 NOT_FOUND: No compensated transaction found
     * - 503 SERVICE_UNAVAILABLE: Order Service still down
     * - 500 INTERNAL_SERVER_ERROR: Unexpected error
     * 
     * Example:
     * POST /payment/recover/25
     * 
     * Success Response (200):
     * {
     *   "status": "RECOVERED",
     *   "message": "Order updated to CANCELLED after payment failure",
     *   "orderId": "25",
     *   "orderStatus": "CANCELLED",
     *   "timestamp": 1763542257531
     * }
     * 
     * Error Response (503):
     * {
     *   "status": "SERVICE_UNAVAILABLE",
     *   "message": "Order Service still unavailable. Please try again later.",
     *   "errorCode": "SERVICE_STILL_DOWN",
     *   "timestamp": 1763542257531
     * }
     */
//	@PreAuthorize("hasAnyRole('CUSTOMER','VENDOR','ADMIN')")
    @PostMapping("/recover/{orderId}")
    public ResponseEntity<?> recoverCompensatedTransaction(@PathVariable String orderId) {
        logger.info("🔄 ╔════════════════════════════════════════╗");
        logger.info("🔄 ║  RECOVERY REQUEST RECEIVED             ║");
        logger.info("🔄 ║  Order ID: {}", orderId);
        logger.info("🔄 ╚════════════════════════════════════════╝");
        
        try {
            // Validate order ID format
            if (orderId == null || orderId.trim().isEmpty()) {
                logger.error("❌ Order ID is null or empty");
                
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "INVALID_REQUEST");
                errorResponse.put("message", "Order ID cannot be null or empty");
                errorResponse.put("errorCode", "INVALID_ORDER_ID");
                errorResponse.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Call recovery service
            PaymentResponse response = paymentService.recoverCompensatedTransaction(orderId);
            logger.info("✅ Order {} recovered successfully", orderId);
            
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("status", "RECOVERED");
            successResponse.put("message", "Order updated to CANCELLED after payment failure");
            successResponse.put("orderId", orderId);
            successResponse.put("orderStatus", response.getOrder().getOrderStatus());
            successResponse.put("transactionId", response.getTransaction().getId());
            successResponse.put("timestamp", System.currentTimeMillis());
            
            logger.info("✅ ╔════════════════════════════════════════╗");
            logger.info("✅ ║  RECOVERY SUCCESSFUL                   ║");
            logger.info("✅ ║  Order {} → CANCELLED                  ║", orderId);
            logger.info("✅ ╚════════════════════════════════════════╝");
            
            return ResponseEntity.ok(successResponse);

        } catch (ResourceNotFoundException e) {
            logger.error("❌ No compensated transaction found: {}", orderId);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "NOT_FOUND");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("errorCode", "NO_COMPENSATED_TX");
            errorResponse.put("details", "No compensated transaction found for order: " + orderId + 
                    ". This order may not have required compensation.");
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (SagaExecutionException e) {
            // Handle service unavailable or other saga errors
            if ("SERVICE_STILL_DOWN".equals(e.getErrorCode())) {
                logger.error("❌ Order Service still unavailable: {}", e.getMessage());
                
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "SERVICE_UNAVAILABLE");
                errorResponse.put("message", e.getMessage());
                errorResponse.put("errorCode", e.getErrorCode());
                errorResponse.put("details", "Order Service is still unreachable. Please restart Order Service and try again.");
                errorResponse.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
            } else {
                logger.error("❌ Recovery failed: {}", e.getMessage());
                
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "FAILED");
                errorResponse.put("message", e.getMessage());
                errorResponse.put("errorCode", e.getErrorCode());
                errorResponse.put("details", "Recovery failed. Please check Order Service status and try again.");
                errorResponse.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

        } catch (Exception e) {
            logger.error("❌ Unexpected error during recovery: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "FAILED");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("errorCode", "RECOVERY_ERROR");
            errorResponse.put("details", "Unexpected error during recovery operation");
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== TRANSACTION QUERY ENDPOINTS ====================

    /**
     * Get all transactions
     * 
     * GET /payment/transactions
     */
//	@PreAuthorize("hasAnyRole('CUSTOMER','VENDOR','ADMIN')")
    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        logger.info("📋 Fetching all transactions");
        List<Transaction> transactions = paymentService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get transaction by ID
     * 
     * GET /payment/transactions/{id}
     * 
     */
//	@PreAuthorize("hasAnyRole('CUSTOMER','VENDOR','ADMIN')")
    @GetMapping("/transactions/{id}")
    public ResponseEntity<?> getTransactionById(@PathVariable UUID id) {
        logger.info("🔍 Fetching transaction: {}", id);
        try {
            Transaction transaction = paymentService.getTransactionById(id);
            return ResponseEntity.ok(transaction);
        } catch (ResourceNotFoundException e) {
            logger.error("❌ Transaction not found: {}", id);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "NOT_FOUND");
            errorResponse.put("message", "Transaction not found with ID: " + id);
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("❌ Error fetching transaction: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get transactions by order ID
     * 
     * GET /payment/transactions/order/{orderId}
     */
//	@PreAuthorize("hasAnyRole('CUSTOMER','VENDOR','ADMIN')")
    @GetMapping("/transactions/order/{orderId}")
    public ResponseEntity<List<Transaction>> getTransactionsByOrderId(@PathVariable String orderId) {
        logger.info("🔍 Fetching transactions for order: {}", orderId);
        List<Transaction> transactions = paymentService.getTransactionsByOrderId(orderId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get transactions by user ID
     * 
     * GET /payment/transactions/user/{userId}
     */
//	@PreAuthorize("hasAnyRole('CUSTOMER','VENDOR','ADMIN')")
    @GetMapping("/transactions/user/{userId}")
    public ResponseEntity<List<Transaction>> getTransactionsByUserId(@PathVariable String userId) {
        logger.info("🔍 Fetching transactions for user: {}", userId);
        List<Transaction> transactions = paymentService.getTransactionsByUserId(userId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get transactions by restaurant ID
     * 
     * GET /payment/transactions/restaurant/{restaurantId}
     */
//	@PreAuthorize("hasAnyRole('CUSTOMER','VENDOR','ADMIN')")
    @GetMapping("/transactions/restaurant/{restaurantId}")
    public ResponseEntity<List<Transaction>> getTransactionsByRestaurantId(@PathVariable String restaurantId) {
        logger.info("🔍 Fetching transactions for restaurant: {}", restaurantId);
        List<Transaction> transactions = paymentService.getTransactionsByRestaurantId(restaurantId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get transactions by status
     * 
     * GET /payment/transactions/status/{status}
     * 
     * Filter by transaction status:
     * - SUCCESS: Payment successful
     * - FAILED:COMPENSATED: Payment failed, compensating TX executed
     * - REFUNDED: Payment refunded
     */
//	@PreAuthorize("hasAnyRole('CUSTOMER','VENDOR','ADMIN')")
    @GetMapping("/transactions/status/{status}")
    public ResponseEntity<List<Transaction>> getTransactionsByStatus(@PathVariable String status) {
        logger.info("🔍 Fetching transactions with status: {}", status);
        List<Transaction> transactions = paymentService.getTransactionsByStatus(status);
        return ResponseEntity.ok(transactions);
    }

    // ==================== HEALTH CHECK ====================

    /**
     * Health check endpoint
     * 
     * GET /payment/health
     */
//	@PreAuthorize("hasAnyRole('CUSTOMER','VENDOR','ADMIN')")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> healthResponse = new HashMap<>();
        healthResponse.put("status", "UP");
        healthResponse.put("service", "Payment Service");
        healthResponse.put("message", "Payment Service is running ✅");
        healthResponse.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(healthResponse);
    }

    // ==================== ENDPOINTS SUMMARY ====================
    
    /**
     * Available Endpoints Summary:
     * 
     * PAYMENT PROCESSING:
     * POST /payment/pay                              - Process payment
     * POST /payment/refund/{id}                      - Refund payment
     * 
     * RECOVERY:
     * POST /payment/recover/{orderId}                - Recover compensated transaction
     * 
     * TRANSACTION QUERIES:
     * GET  /payment/transactions                     - Get all transactions
     * GET  /payment/transactions/{id}                - Get transaction by ID
     * GET  /payment/transactions/order/{orderId}     - Get transactions by order ID
     * GET  /payment/transactions/user/{userId}       - Get transactions by user ID
     * GET  /payment/transactions/restaurant/{restaurantId} - Get by restaurant ID
     * GET  /payment/transactions/status/{status}     - Get transactions by status
     * 
     * HEALTH:
     * GET  /payment/health                           - Health check
     */
}