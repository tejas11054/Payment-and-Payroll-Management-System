package com.paymentapp.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.paymentapp.dto.OrganizationRegisterDTO;
import com.paymentapp.dto.OrganizationResponseDTO;
import com.paymentapp.entity.User;

public interface OrganizationService {

	public OrganizationResponseDTO registerOrganization(OrganizationRegisterDTO dto, MultipartFile[] verificationDocs,boolean reactivate);
	
	void changePassword(Long orgId, String oldPassword, String newPassword, User performingUser);
	
	void requestdeleteOrganization(Long orgId, User performingUser);
		
	OrganizationResponseDTO updateOrganization(Long orgId, OrganizationRegisterDTO dto);

    List<OrganizationResponseDTO> getAllOrganizations();

    OrganizationResponseDTO getOrganizationById(Long orgId);

    BigDecimal getAccountBalancebyOrgId(Long orgId);
    
    OrganizationResponseDTO getOrganizationWithDocuments(Long orgId);

    void addBalance(Long orgId, BigDecimal amount);
}
