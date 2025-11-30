package com.cts.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cts.dto.RestaurantDto;

@FeignClient(name = "RESTAURANT-SERVICE")
public interface RestaurantClient {
    @GetMapping("/api/restaurants/{id}")
    RestaurantDto getRestaurantById(@PathVariable("id") Long restaurantId);
}

