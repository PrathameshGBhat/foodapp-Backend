package com.cts.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.RequestBody;
import com.cts.dtos.InventoryUpdateRequest;

import org.springframework.web.bind.annotation.PutMapping;

 
@FeignClient(name = "inventory-service",configuration = FeignClientInterceptor.class)
public interface InventoryClient {
	 @PutMapping("/api/inventory/update")
	   ResponseEntity<Void> updateInventory(@RequestBody InventoryUpdateRequest request);
}
