package com.cts.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cts.client.RestaurantClient;
import com.cts.client.RestaurantResponse;
import com.cts.dto.MenuItemDto;
import com.cts.exception.CategoryNotFoundException;
import com.cts.exception.DuplicateException;
import com.cts.exception.ExternalServiceUnavailableException;
import com.cts.service.MenuItemService;
import com.cts.validation.Validation.Create;
import com.cts.validation.Validation.Update;

import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/menu")
public class MenuItemController {
	@Autowired
	private MenuItemService menuItemService;
	@Autowired
	private RestaurantClient restaurantClient;
	
	@PreAuthorize("hasAnyRole('ADMIN','VENDOR','CUSTOMER')")
	@GetMapping
	public ResponseEntity<List<MenuItemDto>> getAll() {
		List<MenuItemDto> items = menuItemService.getAllMenuItems();
		return ResponseEntity.ok(items);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN','VENDOR','CUSTOMER')")
	@GetMapping("/id/{id}")
	public ResponseEntity<MenuItemDto> getById(@PathVariable Long id) {
		MenuItemDto item = menuItemService.getMenuItemById(id);
		return ResponseEntity.ok(item);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN','VENDOR','CUSTOMER')")
	@GetMapping("/name/{name}")
	public MenuItemDto getByName(@PathVariable String name) {
		return menuItemService.getMenuItemByName(name);
	}

//	@PostMapping
//	public ResponseEntity<MenuItemDto> create(@Validated(Create.class) @RequestBody MenuItemDto dto) {
//		MenuItemDto created = menuItemService.createMenuItem(dto);
//		return new ResponseEntity<>(created, HttpStatus.CREATED);
//	}
	
	@PreAuthorize("hasAnyRole('ADMIN','VENDOR','CUSTOMER')")
	@GetMapping("restaurant/{id}")
	public ResponseEntity<Map<String, List<MenuItemDto>>> getMenuByRestaurant(@PathVariable Long id) {
		Map<String, List<MenuItemDto>> menu = menuItemService.getMenuByRestaurant(id);
		return ResponseEntity.ok(menu);
	}

//	@GetMapping("/search")
//	public ResponseEntity<MenuItemDto> getMenuItem(
//	        @RequestParam(required = false) Long restaurantId,
//	        @RequestParam(required = false) String restaurantName,
//	        @RequestParam(required = false) Long categoryId,
//	        @RequestParam(required = false) String categoryName,
//	        @RequestParam(required = false) Long itemId,
//	        @RequestParam(required = false) String itemName) {
//
//	    MenuItemDto item = menuItemService.getMenuItemByResturantandCategoryandMenuItem(restaurantId, restaurantName, categoryId, categoryName, itemId, itemName);
//	    return ResponseEntity.ok(item);
//	}
	
@PreAuthorize("hasAnyRole('ADMIN','VENDOR','CUSTOMER')")
@GetMapping("/search")
    public ResponseEntity<MenuItemDto> getMenuItem(
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(required = false) String restaurantName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) String itemName) {

        MenuItemDto item = menuItemService.getMenuItemByRestaurantCategoryAndItem(
                restaurantId, restaurantName, categoryId, categoryName, itemId, itemName);
        return ResponseEntity.ok(item);
    }



@PreAuthorize("hasAnyRole('ADMIN','VENDOR','CUSTOMER')")
	@GetMapping("category/{name}")
	public ResponseEntity<List<MenuItemDto>> getMenuByCategory(@PathVariable String name) {
		List<MenuItemDto> menu = menuItemService.getMenuByCategory(name);
		return ResponseEntity.ok(menu);
	}





@PreAuthorize("hasRole('VENDOR')")
@PostMapping
public ResponseEntity<?> createMenu(@Validated(Create.class) @RequestBody MenuItemDto dto) {
    try {
        MenuItemDto created = menuItemService.createMenuItem(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    } catch (ExternalServiceUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
    } catch (DuplicateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    } catch (CategoryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    } catch (Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body("Something went wrong: " + ex.getMessage());
    }
}


	
	@PreAuthorize("hasRole('VENDOR')")
	@PutMapping("{id}")
	public ResponseEntity<MenuItemDto> update(@PathVariable @Min(1) Long id,
			@Validated(Update.class) @RequestBody MenuItemDto dto) {
		dto.setItemId(id);
		MenuItemDto updated = menuItemService.updateMenuItem(id, dto);
		return new ResponseEntity<MenuItemDto>(updated, HttpStatus.CREATED);
	}
	
	@PreAuthorize("hasRole('VENDOR')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		menuItemService.deleteMenuItem(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

	}

	@PutMapping("/menu/{menuItemId}/availability")
	public ResponseEntity<Void> updateAvailability(
	        @PathVariable Long menuItemId,
	        @RequestParam boolean available) {
	 
	    menuItemService.updateAvailability(menuItemId, available);
	    return ResponseEntity.ok().build();
	}

}
