package com.cts.entities;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    private Integer cartId;
    private Long itemId;
    private Integer quantity;
    private List<CartItemResponse> items;
    private Double totalCartPrice;

   
}