package com.cognizant.paymentservice.model;

import com.cognizant.paymentservice.model.Transaction;
import com.cognizant.paymentservice.model.Wallet;
import com.cognizant.paymentservice.dto.OrderResponseDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for payment processing
 * Contains transaction details, wallet info (if applicable), and order response (if saga successful)
 * 
 * ╔═══════════════════════════════════════════════════════════╗
 * ║          SAGA RESPONSE: Payment Service Output            ║
 * ║    Integrates with CQRS Event Store & Order Service       ║
 * ╚═══════════════════════════════════════════════════════════╝
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {

    @JsonProperty("transactionId")
    private UUID transactionId;  // ✅ Changed to UUID

    @JsonProperty("transaction")
    private Transaction transaction;
    
    @JsonProperty("wallet")
    private Wallet wallet;  // optional, only included for WALLET payments
    
    @JsonProperty("order")
    private OrderResponseDto order;  // optional, included when Order Service is successfully notified

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("responseTimestamp")
    private LocalDateTime responseTimestamp;

    @JsonProperty("sagaStatus")
    private String sagaStatus;  // SUCCESS, PARTIAL_SUCCESS, FAILED

    @JsonProperty("message")
    private String message;

    // ==================== CONSTRUCTORS ====================

    /**
     * Default constructor
     */
    public PaymentResponse() {
        this.responseTimestamp = LocalDateTime.now();
        this.sagaStatus = "UNKNOWN";
    }

    /**
     * Constructor for payment failure or non-wallet payments
     * @param transaction Transaction details
     * @param wallet Wallet (can be null)
     */
    public PaymentResponse(Transaction transaction, Wallet wallet) {
        this();
        this.transaction = transaction;
        this.wallet = wallet;
        this.order = null;
        
        if (transaction != null) {
            this.transactionId = transaction.getId();  // ✅ UUID to UUID
            this.sagaStatus = "SUCCESS".equalsIgnoreCase(transaction.getStatus()) 
                ? "PAYMENT_SUCCESS" 
                : "PAYMENT_FAILED";
            this.message = "Payment processing completed";
        }
    }

    /**
     * Constructor for successful payment with Order Service notification
     * @param transaction Transaction details
     * @param wallet Wallet (can be null)
     * @param order Order response from Order Service
     */
    public PaymentResponse(Transaction transaction, Wallet wallet, OrderResponseDto order) {
        this();
        this.transaction = transaction;
        this.wallet = wallet;
        this.order = order;
        
        if (transaction != null) {
            this.transactionId = transaction.getId();  // ✅ UUID to UUID
        }
        
        // ✅ Determine saga status based on payment and order status
        if (isPaymentSuccessful() && isOrderConfirmed()) {
            this.sagaStatus = "SAGA_SUCCESS";
            this.message = "✅ Payment and Order Confirmation successful (Saga completed)";
        } else if (isPaymentSuccessful()) {
            this.sagaStatus = "PARTIAL_SUCCESS";
            this.message = "⚠️ Payment successful but Order confirmation pending";
        } else {
            this.sagaStatus = "SAGA_FAILED";
            this.message = "❌ Payment failed - Order Service not notified";
        }
    }

    /**
     * Constructor for order response only
     * @param order Order response from Order Service
     */
    public PaymentResponse(OrderResponseDto order) {
        this();
        this.order = order;
        this.transaction = null;
        this.wallet = null;
        
        if (order != null) {
            this.sagaStatus = "ORDER_CONFIRMED";
            this.message = "✅ Order confirmed by Order Service";
        }
    }

    // ==================== GETTERS & SETTERS ====================

    public UUID getTransactionId() {  // ✅ Returns UUID
        return transactionId; 
    }
    public void setTransactionId(UUID transactionId) {  // ✅ Accepts UUID
        this.transactionId = transactionId; 
    }

    public Transaction getTransaction() { 
        return transaction; 
    }
    public void setTransaction(Transaction transaction) { 
        this.transaction = transaction; 
    }

    public Wallet getWallet() { 
        return wallet; 
    }
    public void setWallet(Wallet wallet) { 
        this.wallet = wallet; 
    }

    public OrderResponseDto getOrder() { 
        return order; 
    }
    public void setOrder(OrderResponseDto order) { 
        this.order = order; 
    }

    public LocalDateTime getResponseTimestamp() { 
        return responseTimestamp; 
    }
    public void setResponseTimestamp(LocalDateTime responseTimestamp) { 
        this.responseTimestamp = responseTimestamp; 
    }

    public String getSagaStatus() { 
        return sagaStatus; 
    }
    public void setSagaStatus(String sagaStatus) { 
        this.sagaStatus = sagaStatus; 
    }

    public String getMessage() { 
        return message; 
    }
    public void setMessage(String message) { 
        this.message = message; 
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Check if payment was successful
     */
    public boolean isPaymentSuccessful() {
        return transaction != null && 
               transaction.getStatus() != null &&
               "SUCCESS".equalsIgnoreCase(transaction.getStatus());
    }

    /**
     * Check if order was confirmed
     */
    public boolean isOrderConfirmed() {
        return order != null && 
               order.getOrderStatus() != null &&
               "CONFIRMED".equalsIgnoreCase(order.getOrderStatus());
    }

    /**
     * Check if saga was fully successful (Payment + Order both successful)
     */
    public boolean isSagaSuccessful() {
        return isPaymentSuccessful() && isOrderConfirmed();
    }

    /**
     * Check if payment failed
     */
    public boolean isPaymentFailed() {
        return transaction != null && 
               transaction.getStatus() != null &&
               "FAILED".equalsIgnoreCase(transaction.getStatus());
    }

    /**
     * Check if saga failed
     */
    public boolean isSagaFailed() {
        return "SAGA_FAILED".equalsIgnoreCase(sagaStatus);
    }

    /**
     * Get summary for logging/response
     */
    public String getSummary() {
        return String.format(
            "PaymentResponse{txId=%s, payment=%s, order=%s, wallet=%s, sagaStatus=%s}",
            transaction != null ? transaction.getId() : "NULL",
            transaction != null ? transaction.getStatus() : "NULL",
            order != null ? order.getOrderStatus() : "NULL",
            wallet != null ? wallet.getBalance() : "NULL",
            sagaStatus
        );
    }

    @Override
    public String toString() {
        return "PaymentResponse{" +
                "transactionId=" + transactionId +
                ", transactionStatus=" + (transaction != null ? transaction.getStatus() : null) +
                ", transactionAmount=" + (transaction != null ? transaction.getAmount() : null) +
                ", walletBalance=" + (wallet != null ? wallet.getBalance() : null) +
                ", orderId=" + (order != null ? order.getOrderId() : null) +
                ", orderStatus=" + (order != null ? order.getOrderStatus() : null) +
                ", sagaStatus='" + sagaStatus + '\'' +
                ", responseTimestamp=" + responseTimestamp +
                ", message='" + message + '\'' +
                '}';
    }
}