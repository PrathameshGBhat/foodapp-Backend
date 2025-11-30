package com.cognizant.paymentservice.service;

import com.cognizant.paymentservice.client.OrderClient;
import com.cognizant.paymentservice.dto.OrderResponseDto;
import com.cognizant.paymentservice.exception.ResourceNotFoundException;
import com.cognizant.paymentservice.exception.SagaExecutionException;
import com.cognizant.paymentservice.model.*;
import com.cognizant.paymentservice.repository.TransactionRepository;
import com.cognizant.paymentservice.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import feign.FeignException;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Payment Service - Implements Saga Pattern for Distributed Transactions
 * 
 * Responsibilities:
 * 1. Orchestrate payment processing (Validation → Payment → Transaction Save)
 * 2. Manage inter-service communication with Order Service via Feign
 * 3. Execute compensating transactions on failure
 * 4. Provide transaction recovery after service restoration
 * 5. Provide transaction query operations
 * 6. Handle refunds with saga pattern
 */
@Service
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private OrderClient orderClient;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;
    
 
    // ==================== TRANSACTION QUERY METHODS ====================

    public List<Transaction> getAllTransactions() {
        logger.info("📋 Fetching all transactions");
        return transactionRepository.findAll();
    }

    public Transaction getTransactionById(UUID id) {
        logger.info("🔍 Fetching transaction by ID: {}", id);
        return transactionRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("❌ Transaction not found with ID: {}", id);
                    return new ResourceNotFoundException("Transaction not found with ID: " + id);
                });
    }

    public List<Transaction> getTransactionsByOrderId(String orderId) {
        logger.info("🔍 Fetching transactions for order: {}", orderId);
        return transactionRepository.findByOrderId(orderId);
    }

    public List<Transaction> getTransactionsByUserId(String userId) {
        logger.info("🔍 Fetching transactions for user: {}", userId);
        return transactionRepository.findByUserId(userId);
    }

    public List<Transaction> getTransactionsByRestaurantId(String restaurantId) {
        logger.info("🔍 Fetching transactions for restaurant: {}", restaurantId);
        return transactionRepository.findByRestaurantId(restaurantId);
    }

    public List<Transaction> getTransactionsByStatus(String status) {
        logger.info("🔍 Fetching transactions with status: {}", status);
        return transactionRepository.findByStatus(status);
    }

    // ==================== SAGA ORCHESTRATION ====================

    /**
     * ╔═════════════════════════════════════════════════════════╗
     * ║         SAGA ORCHESTRATOR: processPayment()             ║
     * ╚═════════════════════════════════════════════════════════╝
     * 
     * STEP 1: Validate Payment Request
     * STEP 2: Process Payment
     * STEP 3: Save Transaction (POINT OF NO RETURN)
     * STEP 4: Call Order Service
     *   ✓ If SUCCESS → Return 200 OK
     *   ✗ If FAILED → Execute Compensating Transaction
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        String sagaId = UUID.randomUUID().toString();
        logger.info("🔄 ╔════════════════════════════════════════╗");
        logger.info("🔄 ║  SAGA START: {}  ║", sagaId.substring(0, Math.min(28, sagaId.length())));
        logger.info("🔄 ╚════════════════════════════════════════╝");

        Transaction tx = new Transaction();
        Wallet updatedWallet = null;

        RestaurantInfo restaurant = request.getRestaurant();
        String restaurantId = (restaurant != null) ? restaurant.getId() : null;
        String orderId = (restaurant != null) ? restaurant.getOrderId() : null;

        tx.setOrderId(orderId);
        tx.setUserId(request.getUserId());
        tx.setRestaurantId(restaurantId);
        tx.setAmount(request.getAmount());
        tx.setMethod(request.getMethod());

        // ===== STEP 1: VALIDATION =====
        logger.info("📋 STEP 1: Validating payment request for Order ID: {}", orderId);
        if (!validatePaymentRequest(request, tx, orderId)) {
            logger.error("❌ SAGA FAILED at STEP 1: Validation failed");
            transactionRepository.save(tx);
            return new PaymentResponse(tx, null);
        }
        logger.info("✅ STEP 1 COMPLETE: Validation passed");

        // ===== STEP 2: PROCESS PAYMENT =====
        logger.info("💳 STEP 2: Processing payment by method: {}", request.getMethod());
        PaymentProcessResult paymentResult = processPaymentByMethod(request, tx);
        if (!paymentResult.isSuccess()) {
            logger.error("❌ SAGA FAILED at STEP 2: Payment processing failed");
            transactionRepository.save(tx);
            return new PaymentResponse(tx, paymentResult.getWallet());
        }
        updatedWallet = paymentResult.getWallet();
        tx.setStatus(TransactionStatus.SUCCESS.name());
        logger.info("✅ STEP 2 COMPLETE: Payment processed successfully");

        // ===== STEP 3: SAVE TRANSACTION (POINT OF NO RETURN) =====
        logger.info("💾 STEP 3: Saving transaction to database (POINT OF NO RETURN)");
        Transaction savedTx = transactionRepository.save(tx);
        logger.info("✅ STEP 3 COMPLETE: Transaction saved - ID: {}", savedTx.getId());

        // ===== STEP 4: SAGA ORCHESTRATION - CALL ORDER SERVICE =====
        logger.info("🔗 STEP 4: Initiating SAGA with Order Service for Order ID: {}", orderId);
        try {
            OrderResponseDto orderResponse = notifyOrderStatusChange(orderId, "SUCCESS");
            logger.info("✅ STEP 4 COMPLETE: Order Service successfully updated");
            logger.info("✅ SAGA SUCCESS for Order: {} - Status: {}", orderId, orderResponse.getOrderStatus());
            logger.info("✅ ╔════════════════════════════════════════╗");
            logger.info("✅ ║  SAGA COMPLETED SUCCESSFULLY          ║");
            logger.info("✅ ╚════════════════════════════════════════╝");
            
            return new PaymentResponse(savedTx, updatedWallet, orderResponse);

        } catch (FeignException e) {
            logger.error("❌ SAGA FAILED at STEP 4: Order Service error - {}", e.getMessage());
            logger.warn("⚠️ Executing compensating transaction...");
            
            try {
                executeCompensatingTransaction(savedTx, updatedWallet);
                logger.info("✅ Compensating transaction completed successfully");
                
                throw new SagaExecutionException(
                    "Order Service communication failed - Compensating transaction executed. Wallet refunded and transaction marked COMPENSATED. Call POST /payment/recover/{orderId} after Order Service restarts.", 
                    "FEIGN_ERROR", e);
            } catch (Exception compEx) {
                logger.error("❌ Compensating transaction failed: {}", compEx.getMessage());
                throw new SagaExecutionException(
                    "Order Service communication failed - Compensating transaction also failed", 
                    "COMPENSATING_TX_FAILED", compEx);
            }

        } catch (Exception e) {
            logger.error("❌ SAGA FAILED at STEP 4: Unexpected error - {}", e.getMessage());
            logger.warn("⚠️ Executing compensating transaction...");
            
            try {
                executeCompensatingTransaction(savedTx, updatedWallet);
                throw new SagaExecutionException(
                    "Unexpected error in Saga - Compensating transaction executed", 
                    "UNKNOWN_ERROR", e);
            } catch (Exception compEx) {
                throw new SagaExecutionException(
                    "Unexpected error - Compensating transaction also failed", 
                    "COMPENSATING_TX_FAILED", compEx);
            }
        }
    }

    /**
     * ╔═════════════════════════════════════════════════════════╗
     * ║     COMPENSATING TRANSACTION EXECUTION                  ║
     * ╚═════════════════════════════════════════════════════════╝
     * 
     * When Order Service fails after payment is committed:
     * 
     * Actions:
     * 1. ✅ Mark transaction as COMPENSATED
     * 2. ✅ Refund wallet if applicable
     * 3. ⏭️ DO NOT try to notify Order Service (it's down)
     *    Instead, leave order in PLACED state for manual recovery
     */
    private void executeCompensatingTransaction(Transaction failedTx, Wallet wallet) {
        logger.warn("🔄 ╔════════════════════════════════════════╗");
        logger.warn("🔄 ║  EXECUTING COMPENSATING TRANSACTION    ║");
        logger.warn("🔄 ║  Order ID: " + (failedTx.getOrderId() != null ? failedTx.getOrderId() : "NULL"));
        logger.warn("🔄 ╚════════════════════════════════════════╝");

        // ACTION 1: Mark transaction as COMPENSATED
        logger.info("📝 ACTION 1: Marking transaction as COMPENSATED");
        failedTx.setStatus(TransactionStatus.FAILED.name() + ":COMPENSATED");
        Transaction compensatedTx = transactionRepository.save(failedTx);
        logger.info("✅ ACTION 1 COMPLETE: Transaction marked as compensated - ID: {}", compensatedTx.getId());

        // ACTION 2: Refund wallet
        if ("WALLET".equalsIgnoreCase(failedTx.getMethod()) && wallet != null) {
            logger.info("💰 ACTION 2: Refunding wallet - Amount: {}", failedTx.getAmount());
            wallet.setBalance(wallet.getBalance() + failedTx.getAmount());
            Wallet refundedWallet = walletRepository.save(wallet);
            logger.info("✅ ACTION 2 COMPLETE: Wallet refunded - New Balance: {}", refundedWallet.getBalance());
        } else {
            logger.info("⏭️ ACTION 2 SKIPPED: Not a wallet payment (Method: {})", failedTx.getMethod());
        }

        // ACTION 3: ⏭️ Skip Order Service notification (it's unreachable)
        logger.info("⏭️ ACTION 3 SKIPPED: Order Service is unreachable");
        logger.info("   Order remains in PLACED state");
        logger.info("   Manual recovery required - Use POST /payment/recover/{orderId}");
        logger.info("   This will update the order to CANCELLED once Order Service is back up");

        logger.warn("✅ ╔════════════════════════════════════════╗");
        logger.warn("✅ ║  COMPENSATING TRANSACTION COMPLETE     ║");
        logger.warn("✅ ║  Transaction: COMPENSATED ✅            ║");
        logger.warn("✅ ║  Wallet: REFUNDED ✅                   ║");
        logger.warn("✅ ║  Order: PENDING RECOVERY               ║");
        logger.warn("✅ ║  (Run recovery endpoint when ready)    ║");
        logger.warn("✅ ╚════════════════════════════════════════╝");
    }

    /**
     * ╔═════════════════════════════════════════════════════════╗
     * ║         RECOVERY ENDPOINT - For Compensated Orders      ║
     * ╚═════════════════════════════════════════════════════════╝
     * 
     * When Order Service fails and compensating transaction is executed:
     * 1. ✅ Wallet is refunded
     * 2. ✅ Transaction is marked COMPENSATED
     * 3. ❌ Order remains PLACED (Order Service was unreachable)
     * 
     * Solution: Call this endpoint after Order Service restarts
     * It will update all pending orders to CANCELLED
     * 
     * @param orderId Order ID to recover
     * @return PaymentResponse with updated order status
     */
    public PaymentResponse recoverCompensatedTransaction(String orderId) {
        logger.info("🔄 ╔════════════════════════════════════════╗");
        logger.info("🔄 ║  RECOVERY: Attempting to update order  ║");
        logger.info("🔄 ║  Order ID: {}", orderId);
        logger.info("🔄 ╚════════════════════════════════════════╝");

        try {
            // STEP 1: Validate order ID
            if (orderId == null || orderId.trim().isEmpty()) {
                logger.error("❌ Order ID is null or empty");
                throw new IllegalArgumentException("Order ID cannot be null or empty");
            }

            // STEP 2: Find the compensated transaction for this order
            logger.info("📝 STEP 1: Finding compensated transaction for order: {}", orderId);
            List<Transaction> compensatedTxs = transactionRepository.findByOrderId(orderId);
            
            if (compensatedTxs == null || compensatedTxs.isEmpty()) {
                logger.warn("⚠️ No transactions found for order: {}", orderId);
                throw new ResourceNotFoundException("No transactions found for order: " + orderId);
            }

            Transaction compensatedTx = compensatedTxs.stream()
                .filter(tx -> tx.getStatus() != null && tx.getStatus().contains("COMPENSATED"))
                .findFirst()
                .orElseThrow(() -> {
                    logger.warn("⚠️ No compensated transaction found for order: {}", orderId);
                    return new ResourceNotFoundException("No compensated transaction found for order: " + orderId);
                });

            logger.info("✅ STEP 1 COMPLETE: Found compensated transaction - TX ID: {}", compensatedTx.getId());

            // STEP 3: Now attempt to notify Order Service to CANCEL the order
            logger.info("📤 STEP 2: Attempting to notify Order Service to CANCEL order: {}", orderId);
            OrderResponseDto response = orderClient.updateOrderStatusByPayment(orderId, "FAILED");

            logger.info("✅ STEP 2 COMPLETE: Order Service responded");
            logger.info("   Order ID: {}", response.getOrderId());
            logger.info("   Order Status: {}", response.getOrderStatus());

            logger.info("✅ ╔════════════════════════════════════════╗");
            logger.info("✅ ║  RECOVERY SUCCESSFUL                   ║");
            logger.info("✅ ║  Order {} updated to: {}", orderId, response.getOrderStatus());
            logger.info("✅ ╚════════════════════════════════════════╝");

            return new PaymentResponse(compensatedTx, null, response);

        } catch (IllegalArgumentException e) {
            logger.error("❌ RECOVERY FAILED: Invalid input - {}", e.getMessage());
            throw e;

        } catch (ResourceNotFoundException e) {
            logger.error("❌ RECOVERY FAILED: Resource not found - {}", e.getMessage());
            throw e;

        } catch (FeignException.ServiceUnavailable e) {
            logger.error("❌ RECOVERY FAILED: Order Service still unavailable (503)");
            throw new SagaExecutionException(
                "Order Service still unavailable. Please try again later.",
                "SERVICE_STILL_DOWN", e);

        } catch (FeignException e) {
            logger.error("❌ RECOVERY FAILED: Order Service error - Status: {}, Message: {}", e.status(), e.getMessage());
            throw new SagaExecutionException(
                "Order Service communication failed: " + e.getMessage(),
                "SERVICE_ERROR", e);

        } catch (Exception e) {
            logger.error("❌ RECOVERY FAILED: Unexpected error - {}", e.getMessage(), e);
            throw new SagaExecutionException("Recovery failed: " + e.getMessage(), "RECOVERY_ERROR", e);
        }
    }

    // ==================== VALIDATION ====================

    private boolean validatePaymentRequest(PaymentRequest request, Transaction tx, String orderId) {
        logger.info("🔍 Validating: userId={}, restaurantId={}, orderId={}", 
            request.getUserId(), 
            (request.getRestaurant() != null ? request.getRestaurant().getId() : null),
            orderId);

        if (request.getUserId() == null || request.getUserId().isEmpty()) {
            logger.error("❌ Validation FAILED: Missing User ID");
            tx.setStatus(TransactionStatus.FAILED.name() + ":MISSING_USER_ID");
            return false;
        }

        if (request.getRestaurant() == null || request.getRestaurant().getId() == null) {
            logger.error("❌ Validation FAILED: Missing Restaurant ID");
            tx.setStatus(TransactionStatus.FAILED.name() + ":MISSING_RESTAURANT_ID");
            return false;
        }

        if (orderId == null || orderId.isEmpty()) {
            logger.error("❌ Validation FAILED: Missing Order ID");
            tx.setStatus(TransactionStatus.FAILED.name() + ":MISSING_ORDER_ID");
            return false;
        }

        if (request.getAmount() == null || request.getAmount() <= 0) {
            logger.error("❌ Validation FAILED: Invalid Amount");
            tx.setStatus(TransactionStatus.FAILED.name() + ":INVALID_AMOUNT");
            return false;
        }

        logger.info("✅ Validation PASSED");
        return true;
    }

    // ==================== PAYMENT PROCESSING ====================

    private PaymentProcessResult processPaymentByMethod(PaymentRequest request, Transaction tx) {
        String method = request.getMethod().toUpperCase();
        logger.info("💳 Processing {} payment for amount: {}", method, request.getAmount());

        switch (method) {
            case "UPI":
                return processUPIPayment(request, tx);
            case "CARD":
                return processCardPayment(request, tx);
            case "WALLET":
                return processWalletPayment(request, tx);
            default:
                logger.error("❌ Unknown payment method: {}", method);
                tx.setStatus(TransactionStatus.FAILED.name() + ":UNKNOWN_METHOD");
                return new PaymentProcessResult(false, null);
        }
    }

    private PaymentProcessResult processUPIPayment(PaymentRequest request, Transaction tx) {
        logger.info("🔄 Processing UPI payment...");
        if (request.getUpiId() == null || request.getUpiId().isEmpty()) {
            logger.error("❌ UPI ID is required");
            tx.setStatus(TransactionStatus.FAILED.name() + ":MISSING_UPI_ID");
            return new PaymentProcessResult(false, null);
        }
        logger.info("✅ UPI payment processed successfully");
        return new PaymentProcessResult(true, null);
    }

    private PaymentProcessResult processCardPayment(PaymentRequest request, Transaction tx) {
        logger.info("🔄 Processing Card payment...");
        if (request.getCardNumber() == null || request.getCardExpiry() == null) {
            logger.error("❌ Card details are incomplete");
            tx.setStatus(TransactionStatus.FAILED.name() + ":INCOMPLETE_CARD_DETAILS");
            return new PaymentProcessResult(false, null);
        }
        logger.info("✅ Card payment processed successfully");
        return new PaymentProcessResult(true, null);
    }

    private PaymentProcessResult processWalletPayment(PaymentRequest request, Transaction tx) {
        logger.info("🔄 Processing Wallet payment...");
        try {
            Wallet wallet = walletRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

            if (wallet.getBalance() < request.getAmount()) {
                logger.error("❌ Insufficient wallet balance. Required: {}, Available: {}", 
                    request.getAmount(), wallet.getBalance());
                tx.setStatus(TransactionStatus.FAILED.name() + ":INSUFFICIENT_BALANCE");
                return new PaymentProcessResult(false, wallet);
            }

            wallet.setBalance(wallet.getBalance() - request.getAmount());
            Wallet updatedWallet = walletRepository.save(wallet);
            logger.info("✅ Wallet payment processed. New Balance: {}", updatedWallet.getBalance());
            return new PaymentProcessResult(true, updatedWallet);

        } catch (Exception e) {
            logger.error("❌ Wallet payment failed: {}", e.getMessage());
            tx.setStatus(TransactionStatus.FAILED.name() + ":WALLET_ERROR");
            return new PaymentProcessResult(false, null);
        }
    }

    // ==================== ORDER SERVICE COMMUNICATION ====================

    private OrderResponseDto notifyOrderStatusChange(String orderId, String paymentStatus) {
        if (orderId == null || orderId.isEmpty()) {
            logger.warn("⚠️ Order ID is null/empty, skipping order status update");
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }

        try {
            logger.info("📤 Sending Feign request: PUT /api/orders/{}/update-status?paymentStatus={}", 
                orderId, paymentStatus);
            
            OrderResponseDto response = orderClient.updateOrderStatusByPayment(orderId, paymentStatus);
            
            logger.info("✅ Order Service response received:");
            logger.info("   Order ID: {}", response.getOrderId());
            logger.info("   Order Status: {}", response.getOrderStatus());
            
            return response;

        } catch (FeignException e) {
            logger.error("❌ Feign Exception - Status: {}, Message: {}", e.status(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("❌ Unexpected error during Order Service call: {}", e.getMessage());
            throw new RuntimeException("Failed to notify Order Service", e);
        }
    }

    // ==================== REFUND ====================

	 
	 public PaymentResponse refundPayment(UUID id) {
	     Transaction tx = transactionRepository.findById(id)
	             .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
	
	     Wallet updatedWallet = null;
	     OrderResponseDto orderResponse = null;
	
	     try {
	         // STEP 1: Refund to wallet
	         if ("WALLET".equalsIgnoreCase(tx.getMethod())) {
	             Wallet wallet = walletRepository.findById(tx.getUserId())
	                     .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + tx.getUserId()));
	
	             wallet.setBalance(wallet.getBalance() + tx.getAmount());
	             updatedWallet = walletRepository.save(wallet);
	         }
	
	         // STEP 2: Update transaction status
	         tx.setStatus(TransactionStatus.REFUNDED.name());
	         Transaction savedTx = transactionRepository.save(tx);
	
	         // STEP 3: Notify Order Service of refund (only if orderId exists)
	         if (tx.getOrderId() != null && !tx.getOrderId().isEmpty()) {
	             try {
	                 // Use the correct Feign client method
	                 orderResponse = orderClient.updateOrderStatusByPayment(tx.getOrderId(), "REFUNDED");
	             } catch (Exception e) {
	                 throw new RuntimeException("Order Service notification failed during refund: " + e.getMessage());
	             }
	         }
	
	         return new PaymentResponse(savedTx, updatedWallet, orderResponse);
	
	     } catch (Exception e) {
	         throw new RuntimeException("Refund saga execution failed: " + e.getMessage());
	     }
	 }

    // ==================== HELPER CLASS ====================

    private static class PaymentProcessResult {
        private boolean success;
        private Wallet wallet;

        public PaymentProcessResult(boolean success, Wallet wallet) {
            this.success = success;
            this.wallet = wallet;
        }

        public boolean isSuccess() { 
            return success; 
        }
        
        public Wallet getWallet() { 
            return wallet; 
        }
    }

	public WalletTopUpResponse addMoneyToWallet(@Valid WalletTopUpRequest request) {
		//validateUser(request.getUserId()); // <---- Added

        String userId = request.getUserId();
        double amount = request.getAmount();

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        String method = (request.getMethod() == null) ? "" : request.getMethod().trim().toUpperCase();

        switch (method) {
            case "UPI":
                if (request.getUpiId() == null || request.getUpiId().isBlank())
                    throw new IllegalArgumentException("UPI ID is required");

                if (!request.getUpiId().contains("@"))
                    throw new IllegalArgumentException("UPI ID looks invalid");
                break;

            case "CARD":
                if (request.getCardNumber() == null || request.getCardNumber().isBlank())
                    throw new IllegalArgumentException("Card number is required");

                if (request.getCardExpiry() == null || request.getCardExpiry().isBlank())
                    throw new IllegalArgumentException("Card expiry is required");

                if (request.getCardCvv() == null || request.getCardCvv().isBlank())
                    throw new IllegalArgumentException("Card CVV is required");

                if (!request.getCardNumber().matches("\\d{12,19}"))
                    throw new IllegalArgumentException("Card number looks invalid");

                if (request.getCardCvv().length() < 3 || request.getCardCvv().length() > 4)
                    throw new IllegalArgumentException("Card CVV looks invalid");

                break;

            default:
                throw new IllegalArgumentException("Invalid payment method (UPI, CARD)");
        }

        // Update wallet
        Wallet wallet = walletRepository.findById(userId).orElse(new Wallet());
        wallet.setUserId(userId);
        wallet.setBalance(wallet.getBalance() + amount);

        Wallet savedWallet = walletRepository.save(wallet);

        // Create transaction ONLY for wallet top-up
        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setAmount(amount);
        tx.setMethod(method);
        tx.setStatus(TransactionStatus.SUCCESS.name());
        tx.setOrderId(null);
        tx.setRestaurantId(null);

        Transaction savedTx = transactionRepository.save(tx);

        // Response DTO (without orderId/restaurantId)
        WalletTopUpResponseTransaction txResp = new WalletTopUpResponseTransaction();
        txResp.setId(savedTx.getId());
        txResp.setUserId(savedTx.getUserId());
        txResp.setMethod(savedTx.getMethod());
        txResp.setAmount(savedTx.getAmount());
        txResp.setStatus(savedTx.getStatus());
        txResp.setCreatedAt(savedTx.getCreatedAt());

        return new WalletTopUpResponse(txResp, savedWallet);
    }

	public Wallet getWallet(String userId) {
		return walletRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));
    }
	
}