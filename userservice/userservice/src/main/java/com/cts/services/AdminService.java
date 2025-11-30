package com.cts.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cts.dto.AdminResponse;
import com.cts.entity.Admin;
import com.cts.entity.Role;
import com.cts.repository.AdminRepository;

@Service
public class AdminService {
	
	@Autowired
	private AdminRepository adminRepository;
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private PasswordEncoder encoder;
	
	public AdminResponse register(String name,String email,String password) {
		if(adminRepository.findByEmail(email).isPresent())
			throw new RuntimeException("Admin already exists");
		
		Role role=roleService.getOrCreateRole("ROLE_ADMIN");
		Admin admin=new Admin();
		admin.setName(name);
		admin.setEmail(email);
		admin.setPassword(encoder.encode(password));
		admin.setRole(role);
		
		
		Admin saved=adminRepository.save(admin);
		
		AdminResponse response=new AdminResponse();
		response.setId(saved.getId());
		response.setName(saved.getName());
		response.setEmail(saved.getEmail());
		
		return response;
	}
	
	public Admin login(String email,String password) {
		Admin admin=adminRepository.findByEmail(email).
				orElseThrow(()->new RuntimeException("Invalid email or password"));
		if(!encoder.matches(password, admin.getPassword())) {
			throw new RuntimeException("Invalid email or password");
		}
		return admin;
	}
	
	
}
