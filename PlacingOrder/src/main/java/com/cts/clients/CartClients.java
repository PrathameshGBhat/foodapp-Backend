package com.cts.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import com.cts.entities.CartResponse;

public class CartClients {
	@FeignClient(name = "Cart", configuration = FeignClientInterceptor.class)
	public interface CartClient {

		 @GetMapping("/api/cart/{cartId}")
		 CartResponse getCartById(@PathVariable("cartId") Integer cartId);
	}
}
