package com.cts.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cts.entity.Admin;
import com.cts.entity.Customer;
import com.cts.entity.Vendor;
import com.cts.repository.AdminRepository;
import com.cts.repository.CustomerRepository;
import com.cts.repository.VendorRepository;




@Service
public class CustomUserDetailsService implements UserDetailsService{
	
	 @Autowired
	 private AdminRepository adminRepository;
	 
	 @Autowired
	 private CustomerRepository customerRepository;
	 
	 @Autowired
	 private VendorRepository vendorRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		
		 Optional<Admin> adminOpt=adminRepository.findByEmail(username);
		 if(adminOpt.isPresent()) {
		 	Admin admin=adminOpt.get();
		 	return new CustomUserDetails(admin.getEmail(),admin.getPassword(),"ROLE_ADMIN");
		 }
		 
		 Optional<Customer> customerOpt=customerRepository.findByEmail(username);
		 if(customerOpt.isPresent()) {
		 	Customer customer=customerOpt.get();
		 	return new CustomUserDetails(customer.getEmail(),customer.getPassword(),"ROLE_CUSTOMER");
		 }
		 
		 Optional<Vendor> vendorOpt=vendorRepository.findByEmail(username);
		 if(vendorOpt.isPresent()) {
		 	Vendor vendor=vendorOpt.get();
		 	return new CustomUserDetails(vendor.getEmail(),vendor.getPassword(),"ROLE_VENDOR");
		 }
		
		throw new UsernameNotFoundException("User details service not yet configured");
		
	}
	
}