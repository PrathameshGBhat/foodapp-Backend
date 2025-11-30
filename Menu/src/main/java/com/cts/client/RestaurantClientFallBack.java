package com.cts.client;

import org.springframework.stereotype.Component;




@Component
public class RestaurantClientFallBack implements RestaurantClient {

    @Override
    public RestaurantResponse getRestaurantById(Long id) {
        return new RestaurantResponse(id, "Fallback", "N/A", null, null, "DOWN", null);
    }

    @Override
    public RestaurantResponse getRestaurantByName(String restaurantName) {
        return new RestaurantResponse(null, "Fallback", "N/A", null, null, "DOWN", null);
    }
}



	


