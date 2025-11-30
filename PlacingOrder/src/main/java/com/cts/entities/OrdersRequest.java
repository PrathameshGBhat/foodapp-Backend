package com.cts.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrdersRequest {
    
    @JsonProperty("customerId")
    private Integer customerId;  // ✅ Changed from Long to Integer
    
    @JsonProperty("restaurantId")
    private Integer restaurantId;  // ✅ Changed from Long to Integer
    
    @JsonProperty("cartId")
    private Integer cartId;

    // Getters and Setters
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public Integer getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Integer restaurantId) { this.restaurantId = restaurantId; }

    public Integer getCartId() { return cartId; }
    public void setCartId(Integer cartId) { this.cartId = cartId; }

    @Override
    public String toString() {
        return "OrdersRequest{" +
                "customerId=" + customerId +
                ", restaurantId=" + restaurantId +
                ", cartId=" + cartId +
                '}';
    }
}