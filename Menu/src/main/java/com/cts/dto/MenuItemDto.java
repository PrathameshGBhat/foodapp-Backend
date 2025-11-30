package com.cts.dto;

import com.cts.client.RestaurantResponse;
import com.cts.entity.Category;
import com.cts.validation.Validation.Create;
import com.cts.validation.Validation.Update;

import jakarta.persistence.Id;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MenuItemDto {
	
	//@NotNull(groups = Update.class, message = "Item ID is required for update")
	private Long itemId;

	@NotBlank(message = "Name is required", groups = { Create.class, Update.class })
	@Size(max = 100, message = "Name must be at most 20 characters", groups = { Create.class, Update.class })
	private String itemName;

	@NotBlank(message = "Description is required", groups = { Create.class })
	@Size(max = 500, message = "Description must be at most 500 characters", groups = { Create.class, Update.class })
	private String description;

	@NotNull(message = "Category ID is required", groups = { Create.class, Update.class })
	@Positive(message = "Category ID must be a positive number", groups = { Create.class, Update.class })
	private Long categoryId;

	@NotNull(message = "Price is required", groups = { Create.class, Update.class })
	//the inclusive attribute determines whether the specified minimum value is considered valid or not.
	@DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0", groups = { Create.class,
			Update.class })
	private Double price;
	@NotNull(message = "Restaurant ID is required", groups = { Create.class, Update.class })
	@Positive(message = "Restaurant ID must be a positive number", groups = { Create.class, Update.class })
	 private Long restaurantId;

	@NotNull(message = "Availability status is required", groups = { Create.class, Update.class })
	private Boolean isavailable;
}
