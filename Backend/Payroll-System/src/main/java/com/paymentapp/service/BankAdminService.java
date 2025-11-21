package com.paymentapp.service;

import java.util.List;

import com.paymentapp.dto.OrganizationResponseDTO;
import com.paymentapp.entity.ContactMessage;
import com.paymentapp.entity.Organization;
import com.paymentapp.entity.User;

public interface BankAdminService {
	
	public List<OrganizationResponseDTO> getAllOrganizations();
	
	List<OrganizationResponseDTO> getPendingOrganizations();
	
	Organization approveOrganization(Long orgId, User performingUser);

	Organization rejectOrganization(Long orgId, String reason, User performingUser);
	
	List<OrganizationResponseDTO> getOrganizationsRequestedForDeletion();

	void handleDeletionRequest(Long orgId, boolean approve, String reason, User performingUser);
	
	public List<ContactMessage> getContactMessages();
	
}
