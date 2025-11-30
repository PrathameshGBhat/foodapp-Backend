package com.cts.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cts.entity.Role;
import com.cts.repository.RoleRepository;

@Service
public class RoleService {
	
	@Autowired
	private RoleRepository roleRepository;
	
	public Role getOrCreateRole(String roleName) {
		return roleRepository.findByName(roleName)
				.orElseGet(()->roleRepository.save(new Role(roleName)));
	}

}
