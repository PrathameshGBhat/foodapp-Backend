package com.cts.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cts.dto.VendorResponse;
import com.cts.entity.Role;
import com.cts.entity.Vendor;
import com.cts.repository.VendorRepository;

@Service
public class VendorService {

	@Autowired
	private VendorRepository vendorRepository;
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private PasswordEncoder encoder;
	
	public VendorResponse register(String name,String email,String password) {
		if(vendorRepository.findByEmail(email).isPresent())
			throw new RuntimeException("Vendor already exists");
		
		Role role=roleService.getOrCreateRole("ROLE_VENDOR");
		Vendor vendor=new Vendor();
		vendor.setName(name);
		vendor.setEmail(email);
		vendor.setPassword(encoder.encode(password));
		vendor.setRole(role);

		Vendor saved=vendorRepository.save(vendor);
		
		VendorResponse response=new VendorResponse();
		response.setId(saved.getId());
		response.setName(saved.getName());
		response.setEmail(saved.getEmail());
		
		return response;
	}
	
	public Vendor login(String email,String password) {
		Vendor vendor=vendorRepository.findByEmail(email).
				orElseThrow(()->new RuntimeException("Invalid email or password"));
		if(!encoder.matches(password, vendor.getPassword())) {
			throw new RuntimeException("Invalid email or password");
		}
		return vendor;
	}
	
	public Vendor getVendorById(Long vendorId) {
		return vendorRepository.findById(vendorId).orElse(null);
	}
}
