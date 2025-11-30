package com.cts.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.entity.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Optional<Restaurant> getRestaurantByRestaurantName(String restaurantName);
    boolean existsByRestaurantName(String restaurantName);
    List<Restaurant> findByVendorId(Long vendorId);
}