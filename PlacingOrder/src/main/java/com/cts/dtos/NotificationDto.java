package com.cts.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    private Long orderId;
    private Long vendorId;
    private Long customerId;
    private String type;
    private String message;
    private Double amount;
    private Integer RestaurantId;
    private LocalDateTime Timestamp;
	
}