package com.cts.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cts.dto.VendorResponse;

@FeignClient(name="USERSERVICE",configuration = FeignClientInterceptor.class)
public interface VendorClient {
	
	@GetMapping("/vendor/{vendorId}")
	ResponseEntity<VendorResponse> getVendorById(@PathVariable("vendorId") Long vendorId);

}
