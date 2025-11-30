package com.cts.dtos;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.persistence.Transient;

@JsonPropertyOrder({ "itemId", "restaurantId", "categoryId", "name", "description", "isavailable", "price", "totalItemPrice" })
public class MenuItemDto {
	public MenuItemDto(Long itemId, String name, String description, 
			Double price, Boolean isavailable, Long restaurantId, Long categoryId, Double totalItemPrice
			) {
		super();
		this.itemId = itemId;
		this.name = name;
		this.description = description;
		this.categoryId = categoryId;
		this.price = price;
		this.restaurantId = restaurantId;
		this.isavailable = isavailable;
		this.totalItemPrice = totalItemPrice;
	}

	public MenuItemDto() {
	}

	public Long getItemId() {
		return itemId;
	}

	public Double getTotalItemPrice() {
		return totalItemPrice;
	}

	public void setTotalItemPrice(Double totalItemPrice) {
		this.totalItemPrice = totalItemPrice;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Long getRestaurantId() {
		return restaurantId;
	}

	public void setRestaurantId(Long restaurantId) {
		this.restaurantId = restaurantId;
	}

	public Boolean getIsavailable() {
		return isavailable;
	}

	public void setIsavailable(Boolean isavailable) {
		this.isavailable = isavailable;
	}
	
//	public int getQuantity() {
//		return quantity;
//	}
//
//	public void setQuantity(int quantity) {
//		this.quantity = quantity;
//	}

	private Long itemId;
	private String name;
    private String description;
    private Double price;
    private Boolean isavailable;
    private Long restaurantId;
    private Long categoryId;
    //private int quantity;
    @Transient // Optional if not persisting
    private Double totalItemPrice;
}
