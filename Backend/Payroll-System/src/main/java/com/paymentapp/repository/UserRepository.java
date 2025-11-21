package com.paymentapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paymentapp.entity.Organization;
import com.paymentapp.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
	boolean existsByEmail(String email);
	

	Optional<User> findByUserIdAndDeletedFalse(Long id);
	
	 List<User> findByOrganizationAndRolesRoleNameAndDeletedFalse(Organization organization, String roleName);
	
	 boolean existsByEmailAndUserIdNot(String email, Long userId);
	 
	 boolean existsByEmailIgnoreCaseAndStatus(String email, String status);
	 
	  // ✅ NEW METHOD - Check email within organization scope
	    boolean existsByEmailAndOrganization(String email, Organization organization);
	    
	    // ✅ Check email for users WITHOUT organization (bank admins, etc.)
	    boolean existsByEmailAndOrganizationIsNull(String email);
	    
	    // ✅ Find user by email and organization
	    Optional<User> findByEmailAndOrganization(String email, Organization organization);
	    
	    // ✅ Find user by email (global search for login)
	    Optional<User> findByEmail(String email);
	    
	    
	  
	    
	

}
