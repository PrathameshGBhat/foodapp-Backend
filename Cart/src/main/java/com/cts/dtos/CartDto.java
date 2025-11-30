package com.cts.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "cartId", "itemId", "quantity", "items", "totalCartPrice"})
public class CartDto {
	private int cartId;
	
	@Positive(message = "itemId must be positive")
	@JsonIgnore
	private int itemId;
	
	@NotNull(message = "quantity is required")
	@Positive(message = "quantity must be greater than zero")
	@JsonIgnore
	private int quantity;

	@JsonIgnore
	@PositiveOrZero(message = "totalItemPrice must be zero or positive")
	private double totalItemPrice;
	
	public double getTotalItemPrice() {
		return totalItemPrice;
	}

	public void setTotalItemPrice(double totalItemPrice) {
		this.totalItemPrice = totalItemPrice;
	}

	@PositiveOrZero(message = "totalCartPrice must be zero or positive")
	private double totalCartPrice;
	
	@Transient
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "cart_id") 
	private List<MenuItemDto> items;
	
	public List<MenuItemDto> getItems() {
		return items;
	}

	public void setItems(List<MenuItemDto> items) {
		this.items = items;
	}
	
	private List<CartItemRequestDto> cartItems;
	
	
	public int getCartId() {
		return cartId;
	}

	public void setCartId(int cartId) {
		this.cartId = cartId;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public double getTotalCartPrice() {
		return totalCartPrice;
	}

	public void setTotalCartPrice(double totalCartPrice) {
		this.totalCartPrice = totalCartPrice;
	}

	public List<CartItemRequestDto> getCartItems() {
		return cartItems;
	}

	public void setCartItems(List<CartItemRequestDto> cartItems) {
		this.cartItems = cartItems;
	}

	
}
