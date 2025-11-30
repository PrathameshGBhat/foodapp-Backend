package com.notificationservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.notificationservice.model.RestaurantResponse;

@Service
public class RestaurantClient {

    private final RestTemplate restTemplate = new RestTemplate();

    // configurable base URL for Restaurant service
    @Value("${restaurant.service.url:http://localhost:8081}")
    private String restaurantServiceUrl;

    /**
     * Fetch restaurant info and return RestaurantResponse.
     * Assumes RestaurantService endpoint: GET {restaurantServiceUrl}/api/restaurants/{id}
     */
    public RestaurantResponse getRestaurantById(Integer restaurantId) {
        String url = String.format("%s/api/restaurants/%d", restaurantServiceUrl, restaurantId);
        return restTemplate.getForObject(url, RestaurantResponse.class);
    }

}
