package com.cts.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import com.cts.entity.Role;
import com.cts.repository.RoleRepository;

public class DataLoader implements CommandLineRunner{
	
	@Autowired
	private RoleRepository roleRepository;

	@Override
	public void run(String... args) throws Exception {
		Arrays.asList("ROLE_ADMIN","ROLE_CUSTOMER","ROLE_VENDOR").forEach(role->{
		roleRepository.findByName(role).orElseGet(()->roleRepository.save(new Role(role)));
		
	});
	}

}

