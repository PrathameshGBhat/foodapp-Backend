package com.cts.client;

import com.cts.entity.Category;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponse {
	private Long itemId;
    private String itemName;
    private Boolean isavailable;
    private Long restaurantId;
    private Long categoryId; 
    private String restaurantName;
    private String categoryName;
}
