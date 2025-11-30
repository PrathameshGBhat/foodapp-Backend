package com.cts.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdateItem {
    private Long itemId;     
    private Integer quantity; 
}
