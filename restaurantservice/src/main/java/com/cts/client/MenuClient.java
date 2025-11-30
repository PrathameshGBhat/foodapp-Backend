package com.cts.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "menu-service", url = "http://localhost:8083")
public interface MenuClient {
    @PutMapping("/api/menu/update-availability/{itemId}")
    ResponseEntity<String> updateAvailability(@PathVariable Long itemId, @RequestParam boolean isAvailable);
}

