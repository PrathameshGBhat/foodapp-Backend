package com.cts.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cts.dto.AdminResponse;
import com.cts.dto.LoginRequest;
import com.cts.dto.RegisterRequest;
import com.cts.services.AdminService;



@RestController
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
	private AdminService adminService;
	
	@PostMapping("/register")
	public ResponseEntity<AdminResponse> registerAdmin(@RequestBody RegisterRequest request) {
		AdminResponse admin=adminService.register(request.getName(), request.getEmail(), request.getPassword());
		
		return new ResponseEntity<>(admin,HttpStatus.CREATED);
	}
	
	@PostMapping("/login")
	public ResponseEntity<String> loginAdmin(@RequestBody LoginRequest request) {
		adminService.login(request.getEmail(), request.getPassword());
		
		return ResponseEntity.ok("Admin login successful");
	}
	
		
}