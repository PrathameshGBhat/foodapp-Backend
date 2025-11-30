package com.cts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cts.client.RestaurantClient;
import com.cts.client.RestaurantResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cts.client.RestaurantClient;
import com.cts.client.RestaurantResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;


@Service
public class RestaurantValidatorService {

    @Autowired
    private RestaurantClient restaurantClient;

    @CircuitBreaker(name = "restaurantservice", fallbackMethod = "restaurantFallback")
    public RestaurantResponse validateRestaurant(Long restaurantId) {
        return restaurantClient.getRestaurantById(restaurantId);
    }

    public RestaurantResponse restaurantFallback(Long restaurantId, Throwable throwable) {
        return new RestaurantResponse(restaurantId, "Fallback", "N/A", null, null, "DOWN", null);
    }


}

   
