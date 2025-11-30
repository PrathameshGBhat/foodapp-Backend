package com.cts.entities;

public enum OrderStatus {
    CREATED,
    PAYMENT_PENDING,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    PLACED,
    CONFIRMED,
    PREPARING,
    DELIVERED,
    CANCELLED,
    REFUNDED
}