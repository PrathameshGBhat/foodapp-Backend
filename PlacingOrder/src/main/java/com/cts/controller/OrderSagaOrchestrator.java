package com.cts.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cts.clients.PaymentClient;
import com.cts.entities.OrderStatus;
import com.cts.entities.Orders;
import com.cts.repository.OrderRepository;

@Component
public class OrderSagaOrchestrator {

    @Autowired
    private PaymentClient paymentClient;

    @Autowired
    private OrderRepository orderRepository;
    
    public void processOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        try {
            // Step 1: Initiate Payment
            boolean paymentSuccess = paymentClient.processPayment(order.getOrderId(), order.getSubTotal());

            if (paymentSuccess) {
                // Step 2: Update Order Status
                order.setOrderStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
            } else {
                // Step 3: Compensating Action
                order.setOrderStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
            }
        } catch (Exception ex) {
            // Handle unexpected failures
            order.setOrderStatus(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(order);
        }
    }
}