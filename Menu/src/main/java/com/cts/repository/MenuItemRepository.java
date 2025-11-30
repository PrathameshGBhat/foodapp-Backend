package com.cts.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.cts.entity.MenuItem;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long>, JpaSpecificationExecutor<MenuItem>{
	Optional<MenuItem> findByItemName(String itemname);
	List<MenuItem> findByCategory_CategoryIdAndRestaurantId(Long categoryId, Long restaurantId);
	List<MenuItem> findByCategory_CategoryName(String categoryname);
	 Optional<MenuItem> findByRestaurantIdAndCategory_CategoryIdAndItemId(Long restaurantId, Long categoryId, Long itemId);
	 Boolean existsByItemNameAndRestaurantId(String itemname,Long restaurantId);
}
