package com.cts.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.cts.client.RestaurantClient;
import com.cts.client.RestaurantResponse;
import com.cts.dto.MenuItemDto;
import com.cts.entity.Category;
import com.cts.entity.MenuItem;
import com.cts.exception.CategoryNotFoundException;
import com.cts.exception.DuplicateException;
import com.cts.exception.ExternalServiceUnavailableException;
import com.cts.exception.MenuItemNotFoundException;
import com.cts.exception.RestaurantNotFoundException;
import com.cts.repository.CategoryRepository;
import com.cts.repository.MenuItemRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Service

public class MenuItemServiceImpl implements MenuItemService {
	@Autowired
	private MenuItemRepository menuItemRepository;

	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private RestaurantClient restaurantClient;
	@Autowired
    private RestaurantValidatorService validatorService;


	@Override

	public List<MenuItemDto> getAllMenuItems() {
		List<MenuItem> menuItems = menuItemRepository.findAll();
		return menuItems.stream().map(this::convertMenuItemEntityToDto).collect(Collectors.toList());
	}

	@Override
	public MenuItemDto getMenuItemById(Long id) {
		MenuItem item = menuItemRepository.findById(id)
				.orElseThrow(() -> new MenuItemNotFoundException("Menu item with id " + id + " is not found"));
		return convertMenuItemEntityToDto(item);
	}

	@Override
	public MenuItemDto getMenuItemByName(String name) {
		MenuItem item = menuItemRepository.findByItemName(name)
				.orElseThrow(() -> new MenuItemNotFoundException("Menu item not found"));
		return convertMenuItemEntityToDto(item);
	}
	


@Override
public MenuItemDto createMenuItem(MenuItemDto dto) {
    // Validate category
    Category category = categoryRepository.findById(dto.getCategoryId())
            .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + dto.getCategoryId()));

    // Validate restaurant using Circuit Breaker
    RestaurantResponse restaurant = validatorService.validateRestaurant(dto.getRestaurantId());

    // ✅ Updated validation logic
    if (!restaurant.isSuccess()) {
        throw new ExternalServiceUnavailableException("Restaurant is not available or not open");
    }

    // Check for duplicate menu item
    Boolean exists = menuItemRepository.existsByItemNameAndRestaurantId(dto.getItemName(), dto.getRestaurantId());
    if (exists) {
        throw new DuplicateException("Menu Item already exists in the given restaurant");
    }

    // Create and save menu item
    MenuItem menuItem = new MenuItem();
    menuItem.setItemName(dto.getItemName());
    menuItem.setDescription(dto.getDescription());
    menuItem.setPrice(dto.getPrice());
    menuItem.setIsavailable(dto.getIsavailable());
    menuItem.setCategory(category);

    // ✅ Use restaurantId from RestaurantResponse (not DTO)
    menuItem.setRestaurantId(restaurant.getRestaurantId());

    MenuItem savedMenuItem = menuItemRepository.save(menuItem);
    return convertMenuItemEntityToDto(savedMenuItem);
}




	@Override
	public MenuItemDto updateMenuItem(Long id, MenuItemDto dto) {
		MenuItem item = menuItemRepository.findById(id)
				.orElseThrow(() -> new MenuItemNotFoundException("Menu item with id " + id + " not found"));

		if (dto.getItemName() != null)
			item.setItemName(dto.getItemName());
		if (dto.getDescription() != null)
			item.setDescription(dto.getDescription());
		if (dto.getPrice() != null)
			item.setPrice(dto.getPrice());
		if (dto.getIsavailable() != null)
			item.setIsavailable(dto.getIsavailable());

		if (dto.getCategoryId() != null) {
			Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(
					() -> new CategoryNotFoundException("Category not found with ID: " + dto.getCategoryId()));
			item.setCategory(category);
		}

		MenuItem updated = menuItemRepository.save(item);
		return convertMenuItemEntityToDto(updated);
	}

	@Override
	public Map<String, List<MenuItemDto>> getMenuByRestaurant(Long id) {
		List<Category> categories = categoryRepository.findAll();
		Map<String, List<MenuItemDto>> menuByRestaurantCategory = categories.stream()
			    .collect(Collectors.toMap(
			            Category::getCategoryName,
			            category -> menuItemRepository
			                .findByCategory_CategoryIdAndRestaurantId(category.getCategoryId(), id)
			                .stream()
			                .map(this::convertMenuItemEntityToDto)
			                .collect(Collectors.toList())
			        ));
		return menuByRestaurantCategory;
	}
	

	@Override
	public List<MenuItemDto> getMenuByCategory(String name) {
		List<MenuItem> items = menuItemRepository.findByCategory_CategoryName(name);
		return items.stream().map(this::convertMenuItemEntityToDto).collect(Collectors.toList());
	}


	
	@Override
	public void deleteMenuItem(Long id) {
		if (!menuItemRepository.existsById(id)) {
			throw new MenuItemNotFoundException("Menu item with id " + id + " not found");
		}
		menuItemRepository.deleteById(id);
	}

	public MenuItemDto convertMenuItemEntityToDto(MenuItem item) {
		if (item == null)
			return null;

		Long categoryId = (item.getCategory() != null) ? item.getCategory().getCategoryId() : null;
		return new MenuItemDto(item.getItemId(), item.getItemName(), item.getDescription(), categoryId, item.getPrice(),
				item.getRestaurantId(), item.getIsavailable());
	}

	public MenuItem menuItemDtoToEntity(MenuItemDto dto) {
		if (dto == null)
			return null;

		Category category = null;
		if (dto.getCategoryId() != null) {
			category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(
					() -> new CategoryNotFoundException("Category not found with ID: " + dto.getCategoryId()));
		}

		MenuItem item = new MenuItem();
		item.setItemId(dto.getItemId());
		item.setItemName(dto.getItemName());
		item.setDescription(dto.getDescription());
		item.setPrice(dto.getPrice());
		item.setIsavailable(dto.getIsavailable());
		item.setRestaurantId(dto.getRestaurantId()); // Store restaurant ID
		item.setCategory(category);

		return item;
	}

//	@Override
//	public MenuItemDto getMenuItemByResturantandCategoryandMenuItem(Long restaurantId, String restaurantName,
//	                               Long categoryId, String categoryName,
//	                               Long itemId, String itemName) {
//
//	    Specification<MenuItem> spec = Specification.allOf();
//
//	    if (restaurantId != null) {
//	        spec = spec.and((root, query, cb) -> cb.equal(root.get("restaurantId"), restaurantId));
//	    }
//	    if (restaurantName != null) {
//	        spec = spec.and((root, query, cb) -> cb.equal(root.get("restaurantName"), restaurantName));
//	    }
//	    if (categoryId != null) {
//	        spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("categoryId"), categoryId));
//	    }
//	    if (categoryName != null) {
//	        spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("name"), categoryName));
//	    }
//	    if (itemId != null) {
//	        spec = spec.and((root, query, cb) -> cb.equal(root.get("itemId"), itemId));
//	    }
//	    if (itemName != null) {
//	        spec = spec.and((root, query, cb) -> cb.equal(root.get("name"), itemName));
//	    }
//
//	    MenuItem item = menuItemRepository.findOne(spec)
//	            .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found"));
//
//	    return convertMenuItemEntityToDto(item);
//	}
	@Override
	public MenuItemDto getMenuItemByRestaurantCategoryAndItem(Long restaurantId, String restaurantName,
	                                                          Long categoryId, String categoryName,
	                                                          Long itemId, String itemName) {

	    Long resolvedRestaurantId = restaurantId;

	    if (restaurantName != null && resolvedRestaurantId == null) {
	        RestaurantResponse restaurant = restaurantClient.getRestaurantByName(restaurantName);
	        resolvedRestaurantId = restaurant.getRestaurantId();
	    }

	    Specification<MenuItem> spec = Specification.where(null);

	    if (resolvedRestaurantId != null) {
	        final Long idForSpec = resolvedRestaurantId;
	        spec = spec.and((root, query, cb) -> cb.equal(root.get("restaurantId"), idForSpec));
	    }
	    if (categoryId != null) {
	        spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("categoryId"), categoryId));
	    }
	    if (categoryName != null) {
	        spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("categoryName"), categoryName));
	    }
	    if (itemId != null) {
	        spec = spec.and((root, query, cb) -> cb.equal(root.get("itemId"), itemId));
	    }
	    if (itemName != null) {
	        spec = spec.and((root, query, cb) -> cb.equal(root.get("itemName"), itemName));
	    }

	    MenuItem item = menuItemRepository.findOne(spec)
	            .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found with given criteria"));

	    return convertMenuItemEntityToDto(item);
	}

	@Override
	@Transactional
	public void updateAvailability(Long menuItemId, boolean available) {
	    MenuItem menu = menuItemRepository.findById(menuItemId)
	            .orElseThrow(() -> new RuntimeException("Menu not found"));
	 
	    menu.setIsavailable(available);
	    menuItemRepository.save(menu);
	}


}
