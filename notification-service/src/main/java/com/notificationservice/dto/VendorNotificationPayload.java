package com.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class VendorNotificationPayload {
	private Long orderId;
	private Integer restaurantId;
	private Double subTotal;
	private Long vendorId;

	
}

