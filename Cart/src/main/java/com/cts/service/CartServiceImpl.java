package com.cts.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cts.clients.CartClient;
import com.cts.dtos.CartDto;
import com.cts.dtos.CartItemRequestDto;
import com.cts.dtos.MenuItemDto;
import com.cts.entities.Cart;
import com.cts.entities.CartItem;
import com.cts.repository.CartRepository;

@Service
public class CartServiceImpl implements CartService {
	
	CartRepository cartRepository;
	CartClient cartClient;
	
	public CartServiceImpl(CartRepository cartRepository, CartClient cartClient) {
		super();
		this.cartRepository = cartRepository;
		this.cartClient = cartClient;
	}

	@Override
	public List<CartDto> getAllCarts() {
	    List<Cart> cartEntities = cartRepository.findAll();

	    return cartEntities.stream().map(cart -> {
	        CartDto dto = new CartDto();
	        dto.setCartId(cart.getCartId());
	        dto.setTotalCartPrice(cart.getTotalCartPrice());

	        List<MenuItemDto> items = cart.getItems().stream().map(item -> {
	            MenuItemDto menuItem = new MenuItemDto();
	            menuItem.setItemId(item.getItemId());
	            menuItem.setRestaurantId(item.getRestaurantId());
	            menuItem.setCategoryId(item.getCategoryId());
	            menuItem.setName(item.getName());
	            menuItem.setDescription(item.getDescription());
	            menuItem.setPrice(item.getPrice());
	            menuItem.setIsavailable(item.getIsavailable());
	            menuItem.setTotalItemPrice(item.getTotalItemPrice());
	            return menuItem;
	        }).collect(Collectors.toList());

	        dto.setItems(items);

	        // Restore itemId and quantity at top level safely
	        if (!cart.getItems().isEmpty()) {
	            CartItem firstItem = cart.getItems().get(0);
	            if (firstItem.getItemId() != null) {
	                dto.setItemId(firstItem.getItemId().intValue());
	            }
	            dto.setQuantity(firstItem.getQuantity());
	        }

	        return dto;
	    }).collect(Collectors.toList());
	}
	
	@Override
	public CartDto getCartById(int cartId) {
	    Optional<Cart> cartOpt = cartRepository.findById(cartId);
	    if (cartOpt.isEmpty()) {
	        return null;
	    }

	    Cart cart = cartOpt.get();
	    CartDto dto = new CartDto();
	    dto.setCartId(cart.getCartId());
	    dto.setTotalCartPrice(cart.getTotalCartPrice());

	    List<MenuItemDto> items = cart.getItems().stream().map(item -> {
	        MenuItemDto menuItem = new MenuItemDto();
	        menuItem.setItemId(item.getItemId());
	        menuItem.setRestaurantId(item.getRestaurantId());
	        menuItem.setCategoryId(item.getCategoryId());
	        menuItem.setName(item.getName());
	        menuItem.setDescription(item.getDescription());
	        menuItem.setPrice(item.getPrice());
	        menuItem.setIsavailable(item.getIsavailable());
	        menuItem.setTotalItemPrice(item.getTotalItemPrice());
	        return menuItem;
	    }).collect(Collectors.toList());

	    dto.setItems(items);

	    // Optionally set itemId and quantity from the first item
	    if (!cart.getItems().isEmpty()) {
	        dto.setItemId(cart.getItems().get(0).getItemId().intValue());
	        dto.setQuantity(cart.getItems().get(0).getQuantity());
	    }

	    return dto;
	}
	



//	@Override
//	public CartDto addCart(CartDto cartDto) {
//	    List<CartItemRequestDto> inputItems = cartDto.getCartItems();
//
//	    if (inputItems == null || inputItems.isEmpty()) {
//	        throw new RuntimeException("Cart must contain at least one item.");
//	    }
//
//	    Cart cart = new Cart();
//	    List<CartItem> cartItemList = new ArrayList<>();
//	    double totalCartPrice = 0.0;
//
//	    List<MenuItemDto> enrichedItems = new ArrayList<>();
//
//	    for (CartItemRequestDto inputItem : inputItems) {
//	        Long itemId = inputItem.getItemId();
//	        int quantity = inputItem.getQuantity();
//
//	        MenuItemDto menuItem = cartClient.getMenuItemById(itemId);
//
//	        if (menuItem == null) {
//	            throw new RuntimeException("Menu item not found for ID: " + itemId);
//	        }
//
//	        double totalItemPrice = menuItem.getPrice() * quantity;
//
//	        CartItem item = new CartItem();
//	        item.setItemId(menuItem.getItemId());
//	        item.setCategoryId(menuItem.getCategoryId());
//	        item.setRestaurantId(menuItem.getRestaurantId());
//	        item.setName(menuItem.getName());
//	        item.setDescription(menuItem.getDescription());
//	        item.setPrice(menuItem.getPrice());
//	        item.setIsavailable(menuItem.getIsavailable());
//	        item.setQuantity(quantity);
//	        item.setTotalItemPrice(totalItemPrice);
//	        item.setCart(cart);
//
//	        cartItemList.add(item);
//	        totalCartPrice += totalItemPrice;
//
//	        // Enrich response
//	        menuItem.setTotalItemPrice(totalItemPrice);
//	        enrichedItems.add(menuItem);
//	    }
//
//	    cart.setItems(cartItemList);
//	    cart.setTotalCartPrice(totalCartPrice);
//
//	    Cart savedCart = cartRepository.save(cart);
//
//	    CartDto response = new CartDto();
//	    response.setCartId(savedCart.getCartId());
//	    response.setItems(enrichedItems);
//	    response.setTotalCartPrice(savedCart.getTotalCartPrice());
//
//	    return response;
//	}
	

	@Override
	public CartDto addCart(CartDto cartDto) {
	    List<CartItemRequestDto> inputItems = cartDto.getCartItems();
	
	    if (inputItems == null || inputItems.isEmpty()) {
	        throw new RuntimeException("Cart must contain at least one item.");
	    }
	
	    Cart cart = new Cart();
	    List<CartItem> cartItemList = new ArrayList<>();
	    double totalCartPrice = 0.0;
	
	    List<MenuItemDto> enrichedItems = new ArrayList<>();
	 // Track restaurantId for validation
	    Long restaurantId = null;
	
	    for (CartItemRequestDto inputItem : inputItems) {
	        Long itemId = inputItem.getItemId();
	        int quantity = inputItem.getQuantity();
	
	        MenuItemDto menuItem = cartClient.getById(itemId);
	
	        if (menuItem == null) {
	            throw new RuntimeException("Menu item not found for ID: " + itemId);
	        }
	
	        // Validate restaurant consistency
	        if (restaurantId == null) {
	            restaurantId = menuItem.getRestaurantId();
	        } else if (!restaurantId.equals(menuItem.getRestaurantId())) {
	            throw new RuntimeException("Cart cannot contain items from multiple restaurants.");
	        }
	
	
	        double totalItemPrice = menuItem.getPrice() * quantity;
	
	        CartItem item = new CartItem();
	        item.setItemId(menuItem.getItemId());
	        item.setCategoryId(menuItem.getCategoryId());
	        item.setRestaurantId(menuItem.getRestaurantId());
	        item.setName(menuItem.getName());
	        item.setDescription(menuItem.getDescription());
	        item.setPrice(menuItem.getPrice());
	        item.setIsavailable(menuItem.getIsavailable());
	        item.setQuantity(quantity);
	        item.setTotalItemPrice(totalItemPrice);
	        item.setCart(cart);
	
	        cartItemList.add(item);
	        totalCartPrice += totalItemPrice;
	
	        // Enrich response
	        menuItem.setTotalItemPrice(totalItemPrice);
	        enrichedItems.add(menuItem);
	    }
	
	    cart.setItems(cartItemList);
	    cart.setTotalCartPrice(totalCartPrice);
	
	    Cart savedCart = cartRepository.save(cart);
	
	    CartDto response = new CartDto();
	    response.setCartId(savedCart.getCartId());
	    response.setItems(enrichedItems);
	    response.setTotalCartPrice(savedCart.getTotalCartPrice());
	
	    return response;
	}



	@Override
	public CartDto updateCart(int cartId, CartDto cartDto) {
	    // Fetch the cart
	    Cart cart = cartRepository.findById(cartId)
	        .orElseThrow(() -> new RuntimeException("Cart not found with ID: " + cartId));

	    List<CartItemRequestDto> inputItems = cartDto.getCartItems();
	    if (inputItems == null || inputItems.isEmpty()) {
	        throw new RuntimeException("No items provided for update.");
	    }

	    List<CartItem> updatedItems = new ArrayList<>();
	    List<MenuItemDto> enrichedItems = new ArrayList<>();

	    for (CartItemRequestDto inputItem : inputItems) {
	        Long itemId = inputItem.getItemId();
	        int quantity = inputItem.getQuantity();

	        MenuItemDto menuItem = cartClient.getById(itemId);
	        if (menuItem == null) {
	            throw new RuntimeException("Menu item not found for ID: " + itemId);
	        }

	        Optional<CartItem> existingItemOpt = cart.getItems().stream()
	            .filter(i -> i.getItemId().equals(itemId))
	            .findFirst();

	        if (quantity == 0) {
	            // Remove item if quantity is 0
	            existingItemOpt.ifPresent(cart.getItems()::remove);
	            continue;
	        }

	        double totalItemPrice = menuItem.getPrice() * quantity;

	        if (existingItemOpt.isPresent()) {
	            // Update existing item
	            CartItem existingItem = existingItemOpt.get();
	            existingItem.setQuantity(quantity);
	            existingItem.setTotalItemPrice(totalItemPrice);
	            existingItem.setPrice(menuItem.getPrice());
	            existingItem.setName(menuItem.getName());
	            existingItem.setDescription(menuItem.getDescription());
	            existingItem.setIsavailable(menuItem.getIsavailable());
	            existingItem.setCategoryId(menuItem.getCategoryId());
	            existingItem.setRestaurantId(menuItem.getRestaurantId());
	            updatedItems.add(existingItem);
	        } else {
	            // Add new item
	            CartItem newItem = new CartItem();
	            newItem.setItemId(menuItem.getItemId());
	            newItem.setCategoryId(menuItem.getCategoryId());
	            newItem.setRestaurantId(menuItem.getRestaurantId());
	            newItem.setName(menuItem.getName());
	            newItem.setDescription(menuItem.getDescription());
	            newItem.setPrice(menuItem.getPrice());
	            newItem.setIsavailable(menuItem.getIsavailable());
	            newItem.setQuantity(quantity);
	            newItem.setTotalItemPrice(totalItemPrice);
	            newItem.setCart(cart);
	            cart.getItems().add(newItem);
	            updatedItems.add(newItem);
	        }

	        menuItem.setTotalItemPrice(totalItemPrice);
	        enrichedItems.add(menuItem);
	    }

	    // Recalculate total cart price
	    double updatedTotal = cart.getItems().stream()
	        .mapToDouble(CartItem::getTotalItemPrice)
	        .sum();
	    cart.setTotalCartPrice(updatedTotal);

	    // Save cart
	    Cart savedCart = cartRepository.save(cart);

	    // Prepare response
	    CartDto response = new CartDto();
	    response.setCartId(savedCart.getCartId());
	    response.setItems(enrichedItems);
	    response.setTotalCartPrice(savedCart.getTotalCartPrice());

	    return response;
	}
	
	@Override
	public void deleteCart(int cartId) {
		cartRepository.deleteById(cartId);
		
	}
	
//	@Override
//	public void deleteItemFromCart(int cartId, int itemId) {
//	    Cart cart = cartRepository.findById(cartId)
//	        .orElseThrow(() -> new RuntimeException("Cart not found with ID: " + cartId));
//
//	    // Find the item to remove
//	    CartItem itemToRemove = null;
//	    for (CartItem item : cart.getItems()) {
//	        if (item.getItemId().equals((long) itemId)) {
//	            itemToRemove = item;
//	            break;
//	        }
//	    }
//
//	    if (itemToRemove != null) {
//	        cart.getItems().remove(itemToRemove);
//	    }
//
//	    // Recalculate total cart price
//	    double updatedTotal = cart.getItems().stream()
//	        .mapToDouble(CartItem::getTotalItemPrice)
//	        .sum();
//
//	    cart.setTotalCartPrice(updatedTotal);
//
//	    // Save updated cart
//	    cartRepository.save(cart);
//	}
}
