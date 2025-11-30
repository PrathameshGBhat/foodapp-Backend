package com.cts.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cts.clients.CartClient;
import com.cts.dtos.CartDto;
import com.cts.dtos.MenuItemDto;
import com.cts.service.CartService;

@RestController
@RequestMapping("/api/cart")
public class CartController {
	private CartService cartService;
	private CartClient cartClient;
	
	public CartController(CartService cartService, CartClient cartClient) {
		super();
		this.cartService = cartService;
		this.cartClient = cartClient;
	}
	@PreAuthorize("hasRole('CUSTOMER')")
	@GetMapping
	public ResponseEntity<List<CartDto>> getAll(){
		List<CartDto> carts = cartService.getAllCarts();
		return ResponseEntity.ok(carts);
	}
	@PreAuthorize("hasRole('CUSTOMER')")
	@GetMapping("/{cartId}")
	public ResponseEntity<CartDto> getCartById(@PathVariable int cartId) {
	    CartDto cartDto = cartService.getCartById(cartId);
	    if (cartDto != null) {
	        return ResponseEntity.ok(cartDto);
	    } else {
	        return ResponseEntity.notFound().build();
	    }
	}

	@PreAuthorize("hasRole('CUSTOMER')")
	@PostMapping
	public ResponseEntity<CartDto> createCart(@RequestBody CartDto dto) {
	    // Delegate to service to handle multiple items with individual quantities
	    CartDto savedCart = cartService.addCart(dto);
	    return ResponseEntity.ok(savedCart);
	}

	@PreAuthorize("hasRole('CUSTOMER')")
	@PutMapping("/{cartId}")
	public ResponseEntity<CartDto> updateCart(@PathVariable int cartId, @RequestBody CartDto dto) {
	    CartDto updatedCart = cartService.updateCart(cartId, dto);
	    return ResponseEntity.ok(updatedCart);
	}
	@PreAuthorize("hasRole('CUSTOMER')")
	@DeleteMapping("/{cartId}")
	public ResponseEntity<Void> delete(@PathVariable int cartId) {
		cartService.deleteCart(cartId);
		return ResponseEntity.status(HttpStatus.ACCEPTED).build();
	}
	

//	@DeleteMapping("/{cartId}/{itemId}")
//    public ResponseEntity<Void> deleteItemFromCart(@PathVariable int cartId, @PathVariable int itemId) {
//        cartService.deleteItemFromCart(cartId, itemId);
//        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
//    }

}
