package com.cts.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cts.client.InventoryClient;
import com.cts.client.MenuClient;
import com.cts.client.VendorClient;
import com.cts.dto.InventoryUpdateRequest;
import com.cts.dto.OrderItemDto;
import com.cts.dto.OrdersDto;
import com.cts.dto.RestaurantRequest;
import com.cts.dto.RestaurantResponse;
import com.cts.dto.VendorResponse;
import com.cts.entity.Restaurant;
import com.cts.exception.DuplicateException;
import com.cts.repository.RestaurantRepository;
import com.cts.service.RestaurantService;

import jakarta.transaction.Transactional;

@Service
public class RestaurantServiceImpl implements RestaurantService {

	@Autowired
    private RestaurantRepository repository;
	
	@Autowired
	private VendorClient vendorClient;
	

    @Autowired
    private InventoryClient inventoryClient;

    @Autowired
    private MenuClient menuClient;



    @Override
    @Transactional
    public RestaurantResponse addRestaurant(RestaurantRequest request) {
        String name = request.getRestaurantName().trim();
        if (repository.existsByRestaurantName(name)) {
            throw new DuplicateException("Restaurant name already exists: " + name);
        }

        ResponseEntity<VendorResponse> vendorResponse=vendorClient.getVendorById(request.getVendorId());
        if(vendorResponse==null || !vendorResponse.getStatusCode().is2xxSuccessful() || vendorResponse.getBody()==null) {
        	throw new RuntimeException("Vendor not found with ID: " +request.getVendorId());
        }
        
        Restaurant restaurant=new Restaurant();
        restaurant.setRestaurantName(request.getRestaurantName());
        restaurant.setLocation(request.getLocation());
        restaurant.setOpenTime(request.getOpenTime());
        restaurant.setCloseTime(request.getCloseTime());
        restaurant.setStatus(request.getStatus());
        restaurant.setVendorId(request.getVendorId());
        
        Restaurant saved=repository.save(restaurant);
        return mapToResponse(saved);
    }
    
    @Override
    @Transactional
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        Restaurant existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Restaurant not found"));
        String newName = request.getRestaurantName().trim();
        if (!existing.getRestaurantName().equalsIgnoreCase(newName) && repository.existsByRestaurantName(newName)) {
            throw new DuplicateException("Restaurant name already exists: " + newName);
        }
        
        if(request.getVendorId()!=null) {
        	ResponseEntity<VendorResponse> vendorResponse=vendorClient.getVendorById(request.getVendorId());
        	 if(vendorResponse==null || !vendorResponse.getStatusCode().is2xxSuccessful() || vendorResponse.getBody()==null) {
             	throw new RuntimeException("Vendor not found with ID: " +request.getVendorId());
             }
        	 existing.setVendorId(request.getVendorId());
        }
        existing.setRestaurantName(request.getRestaurantName());
        existing.setLocation(request.getLocation());
        existing.setOpenTime(request.getOpenTime());
        existing.setCloseTime(request.getCloseTime());
        existing.setStatus(request.getStatus());
        Restaurant updated = repository.save(existing);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteRestaurant(Long id) {
    	if(!repository.existsById(id)) {
    		throw new RuntimeException("Restaurant not found with ID: " +id);
    
    	}
        repository.deleteById(id);
    }
    
    @Override
    public List<RestaurantResponse> getAllRestaurants() {
        return repository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant with " + id + " not found"));
        return mapToResponse(restaurant);
    }

    @Override
    public RestaurantResponse getRestaurantByName(String restaurantName) {
        Restaurant restaurant = repository.getRestaurantByRestaurantName(restaurantName)
                .orElseThrow(() -> new RuntimeException("Restaurant with " + restaurantName + " not found"));
        return mapToResponse(restaurant);
    }

    private RestaurantResponse mapToResponse(Restaurant restaurant) {
        return new RestaurantResponse(restaurant.getRestaurantId(), restaurant.getRestaurantName(), restaurant.getLocation(),
                restaurant.getOpenTime(), restaurant.getCloseTime(), restaurant.getStatus(), restaurant.getVendorId());
    }

	@Override
	public List<RestaurantResponse> getRestaurantsByVendor(Long vendorId) {
		return repository.findByVendorId(vendorId)
				.stream().map(this::mapToResponse)
				.collect(Collectors.toList());
		
	}
    @Override
    public void processOrder(OrdersDto order) {
        for (OrderItemDto item : order.getItems()) {
            InventoryUpdateRequest invReq = new InventoryUpdateRequest(
                item.getMenuItemId(),
                order.getRestaurantId().longValue(),
                item.getQuantity()
            );

            ResponseEntity<Integer> invResponse = inventoryClient.updateInventory(invReq);
            int updatedQty = invResponse.getBody();

            if (updatedQty == 0) {
                menuClient.updateAvailability(item.getMenuItemId(), false);
            }
        }
    }
	}