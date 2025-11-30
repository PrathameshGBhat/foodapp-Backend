package com.cognizant.paymentservice.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Wallet {
    
    @Id
    @JsonProperty("userId")
    @Column(name = "user_id")
    private String userId;  // Primary key is userId
    
    @JsonProperty("walletId")
    @Column(name = "wallet_id")
    private String walletId;
    
    @JsonProperty("balance")
    @Column(name = "balance", nullable = false)
    private Double balance = 0.0;
    
    @JsonProperty("currency")
    @Column(name = "currency")
    private String currency;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("lastUpdated")
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // ==================== CONSTRUCTORS ====================

    public Wallet() {
        this.lastUpdated = LocalDateTime.now();
        this.currency = "INR";
    }

    public Wallet(String userId, String walletId, Double balance) {
        this();
        this.userId = userId;
        this.walletId = walletId;
        this.balance = balance;
    }

    // ==================== GETTERS & SETTERS ====================

    public String getUserId() { 
        return userId; 
    }
    public void setUserId(String userId) { 
        this.userId = userId; 
    }

    public String getWalletId() { 
        return walletId; 
    }
    public void setWalletId(String walletId) { 
        this.walletId = walletId; 
    }

    public Double getBalance() { 
        return balance; 
    }
    public void setBalance(Double balance) { 
        this.balance = balance; 
    }

    public String getCurrency() { 
        return currency; 
    }
    public void setCurrency(String currency) { 
        this.currency = currency; 
    }

    public LocalDateTime getLastUpdated() { 
        return lastUpdated; 
    }
    public void setLastUpdated(LocalDateTime lastUpdated) { 
        this.lastUpdated = lastUpdated; 
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "userId='" + userId + '\'' +
                ", walletId='" + walletId + '\'' +
                ", balance=" + balance +
                ", currency='" + currency + '\'' +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}