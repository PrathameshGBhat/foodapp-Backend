package com.cts.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="RESTAURANTSERVICE",configuration = FeignClientInterceptor.class,
fallback = RestaurantClientFallBack.class)
public interface RestaurantClient {

@GetMapping("/api/restaurants/{id}") 
RestaurantResponse getRestaurantById(@PathVariable Long id);

@GetMapping("/api/restaurants/name/{restaurantName}")
RestaurantResponse getRestaurantByName(@PathVariable String restaurantName);
}
