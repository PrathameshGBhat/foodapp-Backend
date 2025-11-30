package com.cts.service;

import java.util.List;
import java.util.Map;

import com.cts.dto.MenuItemDto;

public interface MenuItemService {

	MenuItemDto createMenuItem(MenuItemDto menuItemDTO);

	MenuItemDto updateMenuItem(Long id,MenuItemDto menuItemDTO);

	MenuItemDto getMenuItemById(Long id);
	MenuItemDto getMenuItemByName(String name);
	List<MenuItemDto> getAllMenuItems();
	Map<String, List<MenuItemDto>> getMenuByRestaurant(Long id);
	List<MenuItemDto> getMenuByCategory(String name);
	void deleteMenuItem(Long id);
//	MenuItemDto getMenuItemByResturantandCategoryandMenuItem(Long restaurantId, String restaurantName,
//            Long categoryId, String categoryName,
//            Long itemId, String itemName);
	MenuItemDto getMenuItemByRestaurantCategoryAndItem(Long restaurantId, String restaurantName,
	   Long categoryId, String categoryName,Long itemId, String itemName);

	void updateAvailability(Long menuItemId, boolean available);
}



