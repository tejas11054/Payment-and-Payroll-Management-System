package com.paymentapp.serviceImpl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paymentapp.dto.OrganizationResponseDTO;
import com.paymentapp.entity.ContactMessage;
import com.paymentapp.entity.Organization;
import com.paymentapp.entity.OrganizationVerificationDocument;
import com.paymentapp.entity.User;
import com.paymentapp.repository.ContactMessageRepository;
import com.paymentapp.repository.OrganizationRepository;
import com.paymentapp.repository.OrganizationVerificationDocumentRepository;
import com.paymentapp.service.AuditLogService;
import com.paymentapp.service.BankAdminService;
import com.paymentapp.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankAdminServiceImpl implements BankAdminService {

	private final OrganizationRepository organizationRepository;
	private final NotificationService notificationService;
	private final AuditLogService auditLogService;
	private final ModelMapper modelMapper;
	private final OrganizationVerificationDocumentRepository orgDocRepository;
	private final ContactMessageRepository contactMessageRepository;

	@Override
	public List<OrganizationResponseDTO> getAllOrganizations() {
		List<Organization> approvedOrgs = organizationRepository.findByStatus("APPROVED");
		return approvedOrgs.stream().map(org -> modelMapper.map(org, OrganizationResponseDTO.class)).toList();
	}

	@Override
	public List<OrganizationResponseDTO> getPendingOrganizations() {
		List<Organization> pendingOrgs = organizationRepository.findByStatus("PENDING");
		return pendingOrgs.stream().map(org -> modelMapper.map(org, OrganizationResponseDTO.class)).toList();
	}

	@Override
	@Transactional
	public Organization approveOrganization(Long orgId, User performingUser) {
	    Organization org = organizationRepository.findById(orgId)
	            .orElseThrow(() -> new RuntimeException("Organization not found"));

	    if ("REJECTED".equalsIgnoreCase(org.getStatus())) {
	        throw new RuntimeException("Cannot approve. Organization has already been rejected.");
	    }

	    org.setStatus("APPROVED");
	    organizationRepository.save(org);

	    List<OrganizationVerificationDocument> docs = orgDocRepository.findByOrganization_OrgId(orgId);
	    for (OrganizationVerificationDocument doc : docs) {
	        if ("UPLOADED".equalsIgnoreCase(doc.getStatus())) {
	            doc.setStatus("VERIFIED");
	        }
	    }
	    orgDocRepository.saveAll(docs);

	    notificationService.sendEmail(
	            org.getEmail(),
	            "Organization Approved",
	            "Dear Admin,\n\nYour organization '" + org.getOrgName() +
	            "' has been approved.\n\nYou can now access all features.\n\nBest,\nPaymentApp Team"
	    );

	    auditLogService.log(
	            "APPROVE_ORG",
	            "ORGANIZATION",
	            org.getOrgId(),
	            performingUser.getUserId(),
	            performingUser.getEmail(),
	            "BANK_ADMIN"
	    );

	    return org;
	}


	@Override
	@Transactional
	public Organization rejectOrganization(Long orgId, String reason, User performingUser) {
	    Organization org = organizationRepository.findById(orgId)
	            .orElseThrow(() -> new RuntimeException("Organization not found"));

	    if ("APPROVED".equalsIgnoreCase(org.getStatus())) {
	        throw new RuntimeException("Cannot reject. Organization is already approved.");
	    }

	    org.setStatus("REJECTED");
	    organizationRepository.save(org);

	    List<OrganizationVerificationDocument> docs = orgDocRepository.findByOrganization_OrgId(orgId);
	    for (OrganizationVerificationDocument doc : docs) {
	        doc.setStatus("REJECTED");
	    }
	    orgDocRepository.saveAll(docs);

	    notificationService.sendEmail(
	            org.getEmail(),
	            "Organization Rejected",
	            "Dear Admin,\n\nYour organization '" + org.getOrgName() +
	            "' was rejected.\nReason: " + reason + "\n\nBest,\nPaymentApp Team"
	    );

	    auditLogService.log(
	            "REJECT_ORG",
	            "ORGANIZATION",
	            org.getOrgId(),
	            performingUser.getUserId(),
	            performingUser.getEmail(),
	            "BANK_ADMIN"
	    );

	    return org;
	}



	@Override
	public List<OrganizationResponseDTO> getOrganizationsRequestedForDeletion() {
		List<Organization> deleteRequestedOrgs = organizationRepository.findByStatus("DELETE_REQUESTED");
		return deleteRequestedOrgs.stream().map(org -> modelMapper.map(org, OrganizationResponseDTO.class)).toList();
	}

	@Override
	public void handleDeletionRequest(Long orgId, boolean approve, String reason, User performingUser) {
		Organization org = organizationRepository.findById(orgId)
				.orElseThrow(() -> new RuntimeException("Organization not found"));

		if (approve) {
			org.setDeleted(true);
			org.setStatus("DELETED");

			notificationService.sendEmail(org.getEmail(), "Organization Deleted",
					"Dear Admin,\n\nYour deletion request for '" + org.getOrgName()
							+ "' has been approved and your account is now deactivated.\n\nBest regards,\nPaymentApp Team");

			auditLogService.log("DELETE_ORG", "ORGANIZATION", org.getOrgId(), performingUser.getUserId(),
					performingUser.getEmail(), "BANK_ADMIN");
		} else {
			org.setDeleted(false);
			org.setStatus("APPROVED");

			String rejReason = (reason != null && !reason.isBlank()) ? reason : "No reason provided";

			notificationService.sendEmail(org.getEmail(), "Deletion Request Rejected",
					"Dear Admin,\n\nYour deletion request for '" + org.getOrgName()
							+ "' has been rejected by the Bank Admin.\nReason: " + rejReason
							+ "\n\nBest regards,\nPaymentApp Team");

			auditLogService.log("REJECT_DELETE_ORG", "ORGANIZATION", org.getOrgId(), performingUser.getUserId(),
					performingUser.getEmail(), "BANK_ADMIN");
		}

		organizationRepository.save(org);
	}
	
	@Override
	public List<ContactMessage> getContactMessages() {
		return contactMessageRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
	}

}
