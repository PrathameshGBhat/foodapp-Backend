package com.cts.dtos;

import java.util.List;
import lombok.Data;

@Data
public class InventoryUpdateRequest {
    private Long restaurantId;            
    private List<InventoryUpdateItem> items;
}

