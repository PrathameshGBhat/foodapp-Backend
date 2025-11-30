package com.cts.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrdersDto {
    
    @JsonProperty("orderId")
    private Long orderId;
    
    @JsonProperty("orderStatus")
    private Object orderStatus;
    
    @JsonProperty("customerId")
    private Integer customerId;
    
    @JsonProperty("restaurantId")
    private Integer restaurantId;
    
    @JsonProperty("cartId")
    private Integer cartId;
    
    @JsonProperty("subTotal")
    private Double subTotal;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    @JsonProperty("items")
    private List<OrderItemDto> items = new ArrayList<>();

    // Getters and Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Object getOrderStatus() { return orderStatus; }
    public void setOrderStatus(Object orderStatus) { this.orderStatus = orderStatus; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public Integer getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Integer restaurantId) { this.restaurantId = restaurantId; }

    public Integer getCartId() { return cartId; }
    public void setCartId(Integer cartId) { this.cartId = cartId; }

    public Double getSubTotal() { return subTotal; }
    public void setSubTotal(Double subTotal) { this.subTotal = subTotal; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<OrderItemDto> getItems() { return items; }
    public void setItems(List<OrderItemDto> items) { this.items = items; }

    @Override
    public String toString() {
        return "OrdersDto{" +
                "orderId=" + orderId +
                ", orderStatus=" + orderStatus +
                ", customerId=" + customerId +
                ", restaurantId=" + restaurantId +
                ", cartId=" + cartId +
                ", subTotal=" + subTotal +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", items=" + items +
                '}';
    }
}