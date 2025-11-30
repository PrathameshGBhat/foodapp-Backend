package com.cts.dto;

import com.cts.validation.Validation.Create;
import com.cts.validation.Validation.Update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CategoryDto {

		@NotNull(groups = Update.class, message = "Category ID is required for update")
	    private Long categoryId;

	    @NotBlank(message = "Category name is required", groups = {Create.class, Update.class})
	    @Size(min=5,max = 100, message = "Category name must be at most 100 characters", groups = {Create.class, Update.class})
	    private String categoryName;

	    @Size(min=10,max = 500, message = "Description must be at most 500 characters", groups = {Create.class, Update.class})
	    private String description;
}
