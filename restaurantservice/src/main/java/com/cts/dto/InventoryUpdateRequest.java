package com.cts.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdateRequest {
    private Long itemId;        
    private Long restaurantId;  
    private Integer quantity;  
}

