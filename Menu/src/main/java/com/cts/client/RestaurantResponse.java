package com.cts.client;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantResponse {
	 @JsonProperty("restaurantId")
		private Long restaurantId;
	 @JsonProperty("restaurantName")
	    private String restaurantName;
	 @JsonProperty("location")
		private String location;
	 @JsonProperty("openTime")
		private LocalTime openTime;
	 @JsonProperty("closeTime")
		private LocalTime closeTime;
	 @JsonProperty("status")
	    private String status;
	 @JsonProperty("vendorId")
	    private Long vendorId;

public boolean isSuccess() {
        return restaurantName != null && "OPEN".equalsIgnoreCase(status);
    }

}