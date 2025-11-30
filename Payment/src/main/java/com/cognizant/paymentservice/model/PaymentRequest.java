package com.cognizant.paymentservice.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public class PaymentRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private Double amount;

    @NotBlank(message = "Payment method is required")
    private String method; // CARD or UPI

    private String upiId;

    private String cardNumber;
    private String cardExpiry;
    private String cardCvv;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Restaurant details are required")
    @Valid
    private RestaurantInfo restaurant;

    // ----------------- Conditional validations -----------------
    @AssertTrue(message = "Card number is required")
    private boolean isCardNumberValid() {
        return !"CARD".equalsIgnoreCase(method) || (cardNumber != null && !cardNumber.isBlank());
    }

    @AssertTrue(message = "Card expiry is required")
    private boolean isCardExpiryValid() {
        return !"CARD".equalsIgnoreCase(method) || (cardExpiry != null && !cardExpiry.isBlank());
    }

    @AssertTrue(message = "Card CVV is required")
    private boolean isCardCvvValid() {
        return !"CARD".equalsIgnoreCase(method) || (cardCvv != null && !cardCvv.isBlank());
    }

    @AssertTrue(message = "UPI ID is required")
    private boolean isUpiValid() {
        return !"UPI".equalsIgnoreCase(method) || (upiId != null && !upiId.isBlank());
    }

    // ----------------- Getters and Setters -----------------
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getCardExpiry() { return cardExpiry; }
    public void setCardExpiry(String cardExpiry) { this.cardExpiry = cardExpiry; }

    public String getCardCvv() { return cardCvv; }
    public void setCardCvv(String cardCvv) { this.cardCvv = cardCvv; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public RestaurantInfo getRestaurant() { return restaurant; }
    public void setRestaurant(RestaurantInfo restaurant) { this.restaurant = restaurant; }
}
