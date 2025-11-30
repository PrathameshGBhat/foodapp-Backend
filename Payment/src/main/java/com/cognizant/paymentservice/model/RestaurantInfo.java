package com.cognizant.paymentservice.model;

import jakarta.validation.constraints.NotBlank;

public class RestaurantInfo {

    @NotBlank(message = "Restaurant ID is required")
    private String id;

    @NotBlank(message = "Order ID is required")
    private String orderId;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
}
