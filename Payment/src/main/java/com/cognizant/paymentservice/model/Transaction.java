package com.cognizant.paymentservice.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_order_id", columnList = "order_id"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_restaurant_id", columnList = "restaurant_id"),
    @Index(name = "idx_status", columnList = "status")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @JsonProperty("id")
    private UUID id;  // ✅ UUID type
    
    @JsonProperty("orderId")
    @Column(name = "order_id", nullable = false)
    private String orderId;
    
    @JsonProperty("userId")
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @JsonProperty("restaurantId")
    @Column(name = "restaurant_id", nullable = false)
    private String restaurantId;
    
    @JsonProperty("amount")
    @Column(name = "amount", nullable = false)
    private Double amount;
    
    @JsonProperty("method")
    @Column(name = "method", nullable = false)
    private String method;  // CARD, WALLET, UPI
    
    @JsonProperty("status")
    @Column(name = "status", nullable = false)
    private String status;  // SUCCESS, FAILED, REFUNDED, PENDING
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("transactionDate")
    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;
    
    @JsonProperty("message")
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    
    @JsonProperty("createdAt")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ==================== CONSTRUCTORS ====================

    public Transaction() {
        this.id = UUID.randomUUID();
        this.transactionDate = LocalDateTime.now();
        this.status = "PENDING";
    }

    public Transaction(String orderId, String userId, String restaurantId, Double amount, String method) {
        this();
        this.orderId = orderId;
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.amount = amount;
        this.method = method;
    }

    // ==================== GETTERS & SETTERS ====================

    public UUID getId() {  // ✅ Returns UUID
        return id; 
    }
    public void setId(UUID id) {  // ✅ Accepts UUID
        this.id = id; 
    }

    public String getOrderId() { 
        return orderId; 
    }
    public void setOrderId(String orderId) { 
        this.orderId = orderId; 
    }

    public String getUserId() { 
        return userId; 
    }
    public void setUserId(String userId) { 
        this.userId = userId; 
    }

    public String getRestaurantId() { 
        return restaurantId; 
    }
    public void setRestaurantId(String restaurantId) { 
        this.restaurantId = restaurantId; 
    }

    public Double getAmount() { 
        return amount; 
    }
    public void setAmount(Double amount) { 
        this.amount = amount; 
    }

    public String getMethod() { 
        return method; 
    }
    public void setMethod(String method) { 
        this.method = method; 
    }

    public String getStatus() { 
        return status; 
    }
    public void setStatus(String status) { 
        this.status = status; 
    }

    public LocalDateTime getTransactionDate() { 
        return transactionDate; 
    }
    public void setTransactionDate(LocalDateTime transactionDate) { 
        this.transactionDate = transactionDate; 
    }

    public String getMessage() { 
        return message; 
    }
    public void setMessage(String message) { 
        this.message = message; 
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", orderId='" + orderId + '\'' +
                ", userId='" + userId + '\'' +
                ", restaurantId='" + restaurantId + '\'' +
                ", amount=" + amount +
                ", method='" + method + '\'' +
                ", status='" + status + '\'' +
                ", transactionDate=" + transactionDate +
                ", message='" + message + '\'' +
                '}';
    }

	public LocalDateTime getCreatedAt() {
		// TODO Auto-generated method stub
		return null;
	}
}