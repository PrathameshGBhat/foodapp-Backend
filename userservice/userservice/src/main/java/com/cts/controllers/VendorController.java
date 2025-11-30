package com.cts.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cts.dto.LoginRequest;
import com.cts.dto.RegisterRequest;
import com.cts.dto.VendorResponse;
import com.cts.entity.Vendor;
import com.cts.services.VendorService;

@RestController
@RequestMapping("/vendor")
public class VendorController {
	
	@Autowired
	private VendorService vendorService;
	
	@PostMapping("/register")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<VendorResponse> registerVendor(@RequestBody RegisterRequest request) {
		VendorResponse vendor=vendorService.register(request.getName(), request.getEmail(), request.getPassword());
		
		return new ResponseEntity<>(vendor,HttpStatus.CREATED);
	}
	
	@PostMapping("/login")
	public ResponseEntity<String> loginVendor(@RequestBody LoginRequest request) {
		vendorService.login(request.getEmail(), request.getPassword());
		
		return ResponseEntity.ok("Vendor login successful");
	}
	
	@GetMapping("/{vendorId}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
	public ResponseEntity<?> getVendorById(@PathVariable Long vendorId) {
		
		Vendor vendor=vendorService.getVendorById(vendorId);
		if(vendor==null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vendor not found");
		}
		VendorResponse response=new VendorResponse();
		response.setId(vendor.getId());
		response.setName(vendor.getName());
		response.setEmail(vendor.getEmail());
		
		return ResponseEntity.ok(response);
	}
	
	
	
}