package com.cts.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.cts.dto.InventoryUpdateRequest;


@FeignClient(name = "inventory-service", url = "http://localhost:8082")
public interface InventoryClient {
    @PutMapping("/api/inventory/update")
    ResponseEntity<Integer> updateInventory(@RequestBody InventoryUpdateRequest request);
}


