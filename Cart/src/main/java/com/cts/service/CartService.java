package com.cts.service;

import java.util.List;

import com.cts.dtos.CartDto;

public interface CartService {
	List<CartDto> getAllCarts();
	CartDto getCartById(int cartId);
	CartDto addCart(CartDto cartDto);
	CartDto updateCart(int cartId, CartDto cartDto);
	void deleteCart(int cartId);
	//void deleteItemFromCart(int cartId, int itemId);
}
