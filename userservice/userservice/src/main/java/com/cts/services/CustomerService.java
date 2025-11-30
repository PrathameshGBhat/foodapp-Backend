package com.cts.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cts.dto.AdminResponse;
import com.cts.dto.CustomerResponse;
import com.cts.entity.Customer;
import com.cts.entity.Role;
import com.cts.repository.CustomerRepository;

@Service
public class CustomerService {
	
	@Autowired
	private CustomerRepository customerRepository;
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private PasswordEncoder encoder;
	
	public CustomerResponse register(String name,String email,String password) {
		if(customerRepository.findByEmail(email).isPresent())
			throw new RuntimeException("Customer already exists");
		
		Role role=roleService.getOrCreateRole("ROLE_CUSTOMER");
		Customer customer=new Customer();
		customer.setName(name);
		customer.setEmail(email);
		customer.setPassword(encoder.encode(password));
		customer.setRole(role);
		
		Customer saved=customerRepository.save(customer);
		
		CustomerResponse response=new CustomerResponse();
		response.setId(saved.getId());
		response.setName(saved.getName());
		response.setEmail(saved.getEmail());
		
		return response;
	}
	
	public Customer login(String email,String password) {
		Customer customer=customerRepository.findByEmail(email).
				orElseThrow(()->new RuntimeException("Invalid email or password"));
		if(!encoder.matches(password, customer.getPassword())) {
			throw new RuntimeException("Invalid email or password");
		}
		return customer;
	}

}
