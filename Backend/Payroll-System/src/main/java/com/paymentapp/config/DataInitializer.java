package com.paymentapp.config;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.paymentapp.entity.Role;
import com.paymentapp.entity.User;
import com.paymentapp.repository.RoleRepository;
import com.paymentapp.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional; 
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public void run(String... args) throws Exception {
	
		String adminEmail = "admin@bank.com";
		String adminPassword = "admin@123";


	        if (!userRepository.existsByEmail(adminEmail)) {
	            Role adminRole = roleRepository.findByRoleName("ROLE_BANK_ADMIN")
	                .orElseGet(() -> {
	                    Role newRole = new Role();
	                    newRole.setRoleName("ROLE_BANK_ADMIN");
	                    return roleRepository.save(newRole);
	                });

	            roleRepository.findByRoleName("ROLE_ORGANIZATION")
	                .orElseGet(() -> {
	                    Role newRole = new Role();
	                    newRole.setRoleName("ROLE_ORGANIZATION");
	                    return roleRepository.save(newRole);
	                });
	            
	            
	            User adminUser = new User();
	            adminUser.setEmail(adminEmail);
	            adminUser.setPassword(passwordEncoder.encode(adminPassword));
	            adminUser.setRoles(Set.of(adminRole));

	            userRepository.save(adminUser);

	            System.out.println("======================================================");
	            System.out.println("Default Bank Admin user created successfully!");
	            System.out.println("Email: " + adminEmail);
	            System.out.println("Password: " + adminPassword + " (NOTE: This is the raw password)");
	            System.out.println("======================================================");
	        } else {
	            System.out.println("Bank Admin user already exists. Skipping creation.");
	        }
	    }
}
