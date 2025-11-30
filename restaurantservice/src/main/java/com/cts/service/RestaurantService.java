package com.cts.service;

import java.util.List;

import com.cts.dto.OrdersDto;
import com.cts.dto.RestaurantRequest;
import com.cts.dto.RestaurantResponse;

public interface RestaurantService {
    RestaurantResponse addRestaurant(RestaurantRequest request);
    RestaurantResponse updateRestaurant(Long id, RestaurantRequest request);
    void deleteRestaurant(Long id);
    List<RestaurantResponse> getAllRestaurants();
    RestaurantResponse getRestaurantById(Long id);;
    RestaurantResponse getRestaurantByName(String restaurantName);
    public List<RestaurantResponse> getRestaurantsByVendor(Long vendorId);
    void processOrder(OrdersDto order);
}