package com.paymentapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paymentapp.entity.Organization;
import com.paymentapp.entity.User;
import com.paymentapp.entity.Vendor;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
	List<Vendor> findByOrganizationAndDeletedFalse(Organization organization);

	Optional<Vendor> findByVendorIdAndDeletedFalse(Long vendorId);

	boolean existsByContactEmailAndOrganizationAndDeletedFalse(String contactEmail, Organization organization);

	boolean existsByContactEmailAndOrganizationAndVendorIdNotAndDeletedFalse(String contactEmail,
			Organization organization, Long vendorId);

	Optional<Vendor> findByUserAndDeletedFalse(User user);
}
