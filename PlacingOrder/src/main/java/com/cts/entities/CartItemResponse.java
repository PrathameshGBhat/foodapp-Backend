package com.cts.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {
    private Long itemId;
    private Integer restaurantId;
    private Integer categoryId;
    private String name;
    private String description;
    private Boolean isavailable;
    private Double price;
    private Double totalItemPrice;
    private Integer quantity;

  
}