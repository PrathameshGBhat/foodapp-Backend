package com.cts.dto;

import java.util.List;

import lombok.Data;

@Data
public class OrdersDto {
    private Long orderId;
    private String orderStatus;
    private Integer customerId;
    private Integer restaurantId;
    private Double subTotal;
    private List<OrderItemDto> items;
}

