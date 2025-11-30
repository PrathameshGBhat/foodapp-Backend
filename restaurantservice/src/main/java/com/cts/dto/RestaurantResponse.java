package com.cts.dto;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantResponse {

	
	@NotNull(message = "Restaurant ID is required")
	@Min(value = 1, message = "Restaurant ID must be a positive number")
	private Long restaurantId;

	@Column(unique = true)
	@NotBlank(message = "Restaurant name is required")
	@Size(max = 100, message = "Name must not exceed 100 characters")
	private String restaurantName;

	@NotBlank(message = "Location is required")
	@Size(max = 200, message = "Location must not exceed 200 characters")
	private String location;

	@NotNull(message = "Opening time is required")
	private LocalTime openTime;

	@NotNull(message = "Closing time is required")
	private LocalTime closeTime;

	@NotBlank(message = "Status is required")
	@Pattern(regexp = "OPEN|CLOSED", message = "Status must be either OPEN or CLOSED")
	private String status;
	
	private Long vendorId;
}