package com.cts.dto;

import lombok.Data;

@Data
public class OrderItemDto {
    private Long menuItemId;
    private String name;
    private Double unitPrice;
    private Integer quantity;
}
