package com.cts.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import com.cts.dtos.MenuItemDto;

@FeignClient(name = "menu-service", url = "http://localhost:9002", configuration = FeignClientInterceptor.class )
public interface CartClient {

//    @GetMapping("/api/menu")
//    List<MenuItemDto> getAllMenuItems();

	@GetMapping("/api/menu/id/{id}")
	MenuItemDto getById(@PathVariable Long id);
//
//    @PostMapping("/api/menu")
//    MenuItemDto createMenuItem(MenuItemDto menuItemDTO);
//
//    @PutMapping("/api/menu/{id}")
//    MenuItemDto updateMenuItem(@PathVariable("id") Long id, MenuItemDto menuItemDTO);
//
//    @DeleteMapping("/api/menu/{id}")
//    void deleteMenuItem(@PathVariable("id") Long id);
}