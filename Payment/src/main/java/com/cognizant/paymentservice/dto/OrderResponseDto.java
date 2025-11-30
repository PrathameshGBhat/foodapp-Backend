package com.cognizant.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponseDto {
    
    @JsonProperty("orderId")
    private Long orderId;
    
    @JsonProperty("orderStatus")
    private String orderStatus;
    
    @JsonProperty("customerId")
    private Integer customerId;
    
    @JsonProperty("restaurantId")
    private Integer restaurantId;
    
    @JsonProperty("subTotal")
    private Double subTotal;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    // ==================== CONSTRUCTORS ====================

    public OrderResponseDto() {}

    public OrderResponseDto(Long orderId, String orderStatus, Integer customerId, Double subTotal) {
        this.orderId = orderId;
        this.orderStatus = orderStatus;
        this.customerId = customerId;
        this.subTotal = subTotal;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getOrderId() { 
        return orderId; 
    }
    public void setOrderId(Long orderId) { 
        this.orderId = orderId; 
    }

    public String getOrderStatus() { 
        return orderStatus; 
    }
    public void setOrderStatus(String orderStatus) { 
        this.orderStatus = orderStatus; 
    }

    public Integer getCustomerId() { 
        return customerId; 
    }
    public void setCustomerId(Integer customerId) { 
        this.customerId = customerId; 
    }

    public Integer getRestaurantId() { 
        return restaurantId; 
    }
    public void setRestaurantId(Integer restaurantId) { 
        this.restaurantId = restaurantId; 
    }

    public Double getSubTotal() { 
        return subTotal; 
    }
    public void setSubTotal(Double subTotal) { 
        this.subTotal = subTotal; 
    }

    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }

    public LocalDateTime getUpdatedAt() { 
        return updatedAt; 
    }
    public void setUpdatedAt(LocalDateTime updatedAt) { 
        this.updatedAt = updatedAt; 
    }

    @Override
    public String toString() {
        return "OrderResponseDto{" +
                "orderId=" + orderId +
                ", orderStatus='" + orderStatus + '\'' +
                ", customerId=" + customerId +
                ", restaurantId=" + restaurantId +
                ", subTotal=" + subTotal +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}