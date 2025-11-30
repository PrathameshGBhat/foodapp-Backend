package com.cts.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cts.dto.CustomerResponse;
import com.cts.dto.LoginRequest;
import com.cts.dto.RegisterRequest;
import com.cts.services.CustomerService;

@RestController
@RequestMapping("/customer")
public class CustomerController {
	
	@Autowired
	private CustomerService customerService;
	
	@PostMapping("/register")
	public ResponseEntity<CustomerResponse> registerCustomer(@RequestBody RegisterRequest request) {
		CustomerResponse customer=customerService.register(request.getName(), request.getEmail(), request.getPassword());
		
		return new ResponseEntity<>(customer,HttpStatus.CREATED);
	}
	
	@PostMapping("/login")
	public ResponseEntity<String> loginCustomer(@RequestBody LoginRequest request) {
		customerService.login(request.getEmail(), request.getPassword());
		
		return ResponseEntity.ok("Customer login successful");
	}
	
	
	
	
}