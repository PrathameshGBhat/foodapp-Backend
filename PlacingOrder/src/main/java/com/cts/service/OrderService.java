package com.cts.service;

import java.util.List;
import com.cts.dtos.OrdersDto;
import com.cts.entities.OrdersRequest;

public interface OrderService {
    
    List<OrdersDto> getAllOrders();
    
    OrdersDto getOrdersById(Long orderId);
    
    OrdersDto addOrders(OrdersRequest request);
    
    /**
     * Auto-update order status based on payment status
     * Called directly by Payment Service via Feign
     * 
     * @param orderId Order ID
     * @param paymentStatus Payment status (SUCCESS, FAILED, REFUNDED)
     * @return Updated OrdersDto
     */
    OrdersDto updateOrderStatusByPayment(Long orderId, String paymentStatus);
    
    /**
     * Legacy update - fetches payment status from Payment Service
     */
    void updateOrderStatus(Long orderId);
    
    void deleteOrder(Long orderId);
}