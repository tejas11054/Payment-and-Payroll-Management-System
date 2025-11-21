package com.paymentapp.serviceImpl;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.paymentapp.dto.OrganizationRegisterDTO;
import com.paymentapp.dto.OrganizationResponseDTO;
import com.paymentapp.dto.VerificationDocumentDTO;
import com.paymentapp.entity.BankAdmin;
import com.paymentapp.entity.Organization;
import com.paymentapp.entity.OrganizationVerificationDocument;
import com.paymentapp.entity.Role;
import com.paymentapp.entity.User;
import com.paymentapp.exception.DuplicateBankAccountException;
import com.paymentapp.exception.DuplicateEmailException;
import com.paymentapp.exception.DuplicateOrgNameException;
import com.paymentapp.exception.DuplicatePhoneException;
import com.paymentapp.exception.ResourceNotFoundException;
import com.paymentapp.repository.BankAdminRepository;
import com.paymentapp.repository.OrganizationRepository;
import com.paymentapp.repository.OrganizationVerificationDocumentRepository;
import com.paymentapp.repository.RoleRepository;
import com.paymentapp.repository.UserRepository;
import com.paymentapp.service.AuditLogService;
import com.paymentapp.service.NotificationService;
import com.paymentapp.service.OrganizationService;
import com.paymentapp.service.OrganizationVerificationDocumentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {
	private final OrganizationVerificationDocumentRepository orgVerificationDocRepository;
	private final OrganizationRepository organizationRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final BankAdminRepository bankAdminRepository;
	private final NotificationService notificationService;
	private final OrganizationVerificationDocumentService orgDocService;
	private final RoleRepository roleRepository;
	private final ModelMapper modelMapper;
	private final AuditLogService auditLogService;

	private static final SecureRandom secureRandom = new SecureRandom();

	// OrganizationServiceImpl.java - registerOrganization method

	@Override
	@Transactional
	public OrganizationResponseDTO registerOrganization(
	    OrganizationRegisterDTO dto, 
	    MultipartFile[] verificationDocs, 
	    boolean reactivate
	) {
	    
	    // ═══════════════════════════════════════════════════════════════════
	    // STEP 1: Check Organization-Level Unique Constraints FIRST
	    // ═══════════════════════════════════════════════════════════════════
	    
	    // 1.1 Check Organization Name
	    if (organizationRepository.existsByOrgName(dto.getOrgName())) {
	        throw new DuplicateOrgNameException("Organization name already registered");
	    }
	    
	    // 1.2 Check Organization Phone
	    if (organizationRepository.existsByPhone(dto.getPhone())) {
	        throw new DuplicatePhoneException("Phone number already registered");
	    }
	    
	    // 1.3 Check Bank Account Number
	    if (organizationRepository.existsByBankAccountNo(dto.getBankAccountNo())) {
	        throw new DuplicateBankAccountException("Bank account number already registered");
	    }
	    
	    // ═══════════════════════════════════════════════════════════════════
	    // STEP 2: Check Organization Email (with Reactivation Logic)
	    // ═══════════════════════════════════════════════════════════════════
	    
	    Optional<Organization> existingOrgOpt = organizationRepository.findByEmail(dto.getEmail());
	    
	    if (existingOrgOpt.isPresent()) {
	        Organization existingOrg = existingOrgOpt.get();
	        
	        // 2.1 Handle existing APPROVED organization
	        if ("APPROVED".equalsIgnoreCase(existingOrg.getStatus()) && !existingOrg.isDeleted()) {
	            throw new DuplicateEmailException("Organization email already registered");
	        }
	        
	        // 2.2 Handle PENDING organization (still waiting for approval)
	        if ("PENDING".equalsIgnoreCase(existingOrg.getStatus()) && !existingOrg.isDeleted()) {
	            throw new DuplicateEmailException("Organization email already registered and pending approval");
	        }
	        
	        // 2.3 Handle DELETED organization - Ask for reactivation
	        if (("DELETED".equalsIgnoreCase(existingOrg.getStatus()) || existingOrg.isDeleted()) && !reactivate) {
	            throw new RuntimeException("Organization exists but is deleted. Confirm reactivation?");
	        }
	        
	        // 2.4 Reactivate deleted organization
	        if (("DELETED".equalsIgnoreCase(existingOrg.getStatus()) || existingOrg.isDeleted()) && reactivate) {
	            return reactivateOrganization(existingOrg);
	        }
	    }
	    
	    // ═══════════════════════════════════════════════════════════════════
	    // STEP 3: Check User Email Constraints
	    // ═══════════════════════════════════════════════════════════════════
	    
	    // 3.1 Check if email is used by Bank Admin or System User (no organization)
	    if (userRepository.existsByEmailAndOrganizationIsNull(dto.getEmail())) {
	        throw new DuplicateEmailException("Email is already used by a system administrator");
	    }
	    
	    // ═══════════════════════════════════════════════════════════════════
	    // STEP 4: Create Organization Entity
	    // ═══════════════════════════════════════════════════════════════════
	    
	    Organization org = new Organization();
	    org.setOrgName(dto.getOrgName());
	    org.setEmail(dto.getEmail());
	    org.setPhone(dto.getPhone());
	    org.setAddress(dto.getAddress());
	    org.setBankAccountNo(dto.getBankAccountNo());
	    org.setIfscCode(dto.getIfscCode());
	    org.setBankName(dto.getBankName());
	    org.setEmployeeCount(dto.getEmployeeCount());
	    org.setAccountBalance(BigDecimal.ZERO);
	    org.setStatus("PENDING");
	    org.setDeleted(false);
	    
	    // Save organization first to get orgId
	    org = organizationRepository.save(org);
	    
	    // ═══════════════════════════════════════════════════════════════════
	    // STEP 5: Create Organization User Account
	    // ═══════════════════════════════════════════════════════════════════
	    
	    // 5.1 Generate default password
	    String defaultPassword = generatePasswordFromOrgName(dto.getOrgName());
	    
	    // 5.2 Create user entity
	    User user = new User();
	    user.setEmail(dto.getEmail());
	    user.setPassword(passwordEncoder.encode(defaultPassword));
	    user.setStatus("ACTIVE");
	    user.setDeleted(false);
	    user.setOrganization(org); // ✅ Link to organization
	    
	    // 5.3 Assign ROLE_ORGANIZATION
	    Role role = roleRepository.findByRoleName("ROLE_ORGANIZATION")
	        .orElseGet(() -> {
	            Role r = new Role();
	            r.setRoleName("ROLE_ORGANIZATION");
	            return roleRepository.save(r);
	        });
	    
	    Set<Role> roles = new HashSet<>();
	    roles.add(role);
	    user.setRoles(roles);
	    
	    // 5.4 Save user (no duplicate error due to composite constraint)
	    user = userRepository.save(user);
	    
	    // 5.5 Link user to organization
	    org.getUsers().add(user);
	    
	    // ═══════════════════════════════════════════════════════════════════
	    // STEP 6: Upload Verification Documents
	    // ═══════════════════════════════════════════════════════════════════
	    
	    if (verificationDocs != null && verificationDocs.length > 0) {
	        for (MultipartFile doc : verificationDocs) {
	            try {
	                orgDocService.uploadVerificationDocument(
	                    org.getOrgId(), 
	                    doc, 
	                    "ORG_VERIFICATION_DOC", 
	                    user.getUserId()
	                );
	            } catch (Exception e) {
	                // Log error but don't fail registration
	                System.err.println("Failed to upload document: " + doc.getOriginalFilename());
	                e.printStackTrace();
	            }
	        }
	    }
	    
	    // ═══════════════════════════════════════════════════════════════════
	    // STEP 7: Send Email Notification
	    // ═══════════════════════════════════════════════════════════════════
	    
	    try {
	        notificationService.sendEmail(
	            dto.getEmail(), 
	            "Organization Registration Successful", 
	            String.format("""
	                Dear Admin,
	                
	                Thank you for registering your organization '%s' with PaymentApp.
	                Your verification documents have been uploaded and are pending approval by our Bank Admin.
	                
	                Your default login credentials:
	                Email: %s
	                Password: %s
	                
	                ⚠️ IMPORTANT: Please change your password after first login for security.
	                
	                Once approved, you will receive a confirmation email.
	                
	                Best regards,
	                PaymentApp Team
	                """, 
	                dto.getOrgName(), 
	                dto.getEmail(),
	                defaultPassword
	            )
	        );
	    } catch (Exception e) {
	        // Log error but don't fail registration
	        System.err.println("Failed to send email: " + e.getMessage());
	        e.printStackTrace();
	    }
	    
	    // ═══════════════════════════════════════════════════════════════════
	    // STEP 8: Log Audit Trail
	    // ═══════════════════════════════════════════════════════════════════
	    
	    try {
	        auditLogService.log(
	            "REGISTER_ORG",
	            "ORGANIZATION",
	            org.getOrgId(),
	            user.getUserId(),
	            user.getEmail(),
	            user.getRoles().stream()
	                .findFirst()
	                .map(Role::getRoleName)
	                .orElse("UNKNOWN")
	        );
	    } catch (Exception e) {
	        // Log error but don't fail registration
	        System.err.println("Failed to log audit: " + e.getMessage());
	    }
	    
	    // ═══════════════════════════════════════════════════════════════════
	    // STEP 9: Return Response DTO
	    // ═══════════════════════════════════════════════════════════════════
	    
	    return modelMapper.map(org, OrganizationResponseDTO.class);
	}


	private String generatePasswordFromOrgName(String orgName) {
		String prefix = orgName.length() >= 5 ? orgName.substring(0, 5) : orgName;
		StringBuilder sb = new StringBuilder(prefix);
		sb.append("@");
		SecureRandom random = new SecureRandom();
		for (int i = 0; i < 5; i++) {
			int digit = random.nextInt(10);
			sb.append(digit);
		}
		return sb.toString();
	}

	@Override
	public void changePassword(Long orgId, String oldPassword, String newPassword, User performingUser) {
		Organization org = organizationRepository.findByOrgIdAndDeletedFalse(orgId)
				.orElseThrow(() -> new RuntimeException("Organization not found"));

		User user = org.getUsers().stream().filter(u -> !u.isDeleted()).findFirst()
				.orElseThrow(() -> new RuntimeException("Active user not found for organization"));

		if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
			throw new RuntimeException("Old password is incorrect");
		}

		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		notificationService.sendEmail(user.getEmail(), "Password Changed Successfully", "Dear User,\n\n"
				+ "This is to inform you that the password for your organization account associated with PaymentApp has been changed successfully.\n\n"
				+ "If you did not initiate this change, please contact our support team immediately.\n\n"
				+ "Best regards,\n" + "PaymentApp Security Team");

		auditLogService.log("CHANGE_PASSWORD", "ORGANIZATION", org.getOrgId(), performingUser.getUserId(),
				performingUser.getEmail(),
				performingUser.getRoles().stream().findFirst().map(r -> r.getRoleName()).orElse("UNKNOWN"));

	}
	private OrganizationResponseDTO reactivateOrganization(Organization org) {
	    
	    // Update organization status
	    org.setStatus("APPROVED");
	    org.setDeleted(false);
	    org.setUpdatedAt(Instant.now());
	    
	    organizationRepository.save(org);
	    
	    // Reactivate associated users
	    org.getUsers().forEach(user -> {
	        if (user.isDeleted()) {
	            user.setDeleted(false);
	            user.setStatus("ACTIVE");
	            userRepository.save(user);
	        }
	    });
	    
	    // Send reactivation email
	    try {
	        notificationService.sendEmail(
	            org.getEmail(),
	            "Organization Reactivated Successfully",
	            String.format("""
	                Dear Admin,
	                
	                Your organization '%s' has been successfully reactivated on PaymentApp.
	                
	                You can now log in using your previous credentials.
	                
	                If you forgot your password, please use the 'Forgot Password' option.
	                
	                Best regards,
	                PaymentApp Team
	                """,
	                org.getOrgName()
	            )
	        );
	    } catch (Exception e) {
	        System.err.println("Failed to send reactivation email: " + e.getMessage());
	    }
	    
	    // Log audit
	    try {
	        auditLogService.log(
	            "REACTIVATE_ORG",
	            "ORGANIZATION",
	            org.getOrgId(),
	            null,
	            org.getEmail(),
	            "ROLE_ORGANIZATION"
	        );
	    } catch (Exception e) {
	        System.err.println("Failed to log audit: " + e.getMessage());
	    }
	    
	    return modelMapper.map(org, OrganizationResponseDTO.class);
	}

	@Override
	public void requestdeleteOrganization(Long orgId, User performingUser) {
		Organization org = organizationRepository.findByOrgIdAndDeletedFalse(orgId)
				.orElseThrow(() -> new RuntimeException("Organization not found"));

		org.setStatus("DELETE_REQUESTED");

		organizationRepository.save(org);

		List<BankAdmin> admins = bankAdminRepository.findAll();
		System.out.println("Admins found: " + admins.size());

		for (BankAdmin admin : admins) {
			if (admin.getUser() != null) {
				String adminEmail = admin.getUser().getEmail();
				System.out.println("Sending email to Bank Admin: " + adminEmail);

				notificationService.sendEmail(adminEmail, "Organization Deletion Request Received", """
						Dear Bank Admin,

						The organization '%s' has requested to delete their account from PaymentApp.

						Please log in to the admin panel to review and approve/reject this request.

						Organization Details:
						- Name: %s
						- Email: %s
						- Phone: %s

						Best regards,
						PaymentApp System
						""".formatted(org.getOrgName(), org.getOrgName(), org.getEmail(), org.getPhone()));
			} else {
				System.out.println("BankAdmin has no user associated!");
			}
		}

		notificationService.sendEmail(org.getEmail(), "Your Deletion Request Has Been Submitted", """
				Dear Admin,

				We have received your request to delete your organization account '%s' from PaymentApp.

				Our Bank Admin team will review the request and you will be notified once it is approved or rejected.

				If you did not make this request, please contact our support team immediately.

				Best regards,
				PaymentApp Team
				""".formatted(org.getOrgName()));

		User user = org.getUsers().stream().filter(u -> !u.isDeleted()).findFirst()
				.orElseThrow(() -> new RuntimeException("Active user not found for organization"));

		user.setStatus("INACTIVE");
		userRepository.save(user);

		auditLogService.log("REQUEST_DELETE_ORG", "ORGANIZATION", org.getOrgId(), performingUser.getUserId(),
				performingUser.getEmail(),
				performingUser.getRoles().stream().findFirst().map(r -> r.getRoleName()).orElse("UNKNOWN"));
	}


	@Override
	@Transactional
	public OrganizationResponseDTO updateOrganization(Long orgId, OrganizationRegisterDTO dto) {
	    Organization org = organizationRepository.findByOrgIdAndDeletedFalse(orgId)
	            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

	    User performingUser = org.getUsers().stream()
	            .filter(user -> !user.isDeleted())
	            .findFirst()
	            .orElseThrow(() -> new ResourceNotFoundException("No active user found for the organization"));
	    
	    // ✅ Check unique constraints for UPDATE (excluding current org)
	    
	    // Check Organization Name
	    if (dto.getOrgName() != null && !dto.getOrgName().equals(org.getOrgName())) {
	        if (organizationRepository.existsByOrgNameAndOrgIdNot(dto.getOrgName(), orgId)) {
	            throw new DuplicateOrgNameException("Organization name already registered");
	        }
	        org.setOrgName(dto.getOrgName());
	    }
	    
	    // Check Email
	    if (dto.getEmail() != null && !dto.getEmail().equals(org.getEmail())) {
	        if (organizationRepository.existsByEmailAndOrgIdNot(dto.getEmail(), orgId)) {
	            throw new DuplicateEmailException("Email already registered");
	        }
	        org.setEmail(dto.getEmail());
	    }
	    
	    // Check Phone
	    if (dto.getPhone() != null && !dto.getPhone().equals(org.getPhone())) {
	        if (organizationRepository.existsByPhoneAndOrgIdNot(dto.getPhone(), orgId)) {
	            throw new DuplicatePhoneException("Phone number already registered");
	        }
	        org.setPhone(dto.getPhone());
	    }
	    
	    // Check Bank Account
	    if (dto.getBankAccountNo() != null && !dto.getBankAccountNo().equals(org.getBankAccountNo())) {
	        if (organizationRepository.existsByBankAccountNoAndOrgIdNot(dto.getBankAccountNo(), orgId)) {
	            throw new DuplicateBankAccountException("Bank account number already registered");
	        }
	        org.setBankAccountNo(dto.getBankAccountNo());
	    }
	    
	    // Update other fields
	    if (dto.getAddress() != null) {
	        org.setAddress(dto.getAddress());
	    }
	    if (dto.getIfscCode() != null) {
	        org.setIfscCode(dto.getIfscCode());
	    }
	    if (dto.getBankName() != null) {
	        org.setBankName(dto.getBankName());
	    }
	    if (dto.getEmployeeCount() > 0) {
	        org.setEmployeeCount(dto.getEmployeeCount());
	    }
	    if (dto.getAccountBalance() != null) {
	        org.setAccountBalance(dto.getAccountBalance());
	    }

	    organizationRepository.save(org);
	    
	    auditLogService.log(
	            "UPDATE_ORG",
	            "ORGANIZATION",
	            org.getOrgId(),
	            performingUser.getUserId(),
	            performingUser.getEmail(),
	            performingUser.getRoles().stream().findFirst().map(Role::getRoleName).orElse("UNKNOWN")
	    );
	    
	    notificationService.sendEmail(org.getEmail(),
	            "Organization Details Updated",
	            """
	            Dear Admin,

	            Your organization details have been successfully updated on PaymentApp.

	            If you did not perform this change, please contact support immediately.

	            Best regards,
	            PaymentApp Team
	            """
	    );

	    return modelMapper.map(org, OrganizationResponseDTO.class);
	}

	@Override
	public List<OrganizationResponseDTO> getAllOrganizations() {
		return organizationRepository.findAll().stream().filter(org -> !org.isDeleted())
				.map(org -> modelMapper.map(org, OrganizationResponseDTO.class)).toList();
	}

	@Override
	public OrganizationResponseDTO getOrganizationById(Long orgId) {
		Organization org = organizationRepository.findByOrgIdAndDeletedFalse(orgId)
				.orElseThrow(() -> new RuntimeException("Organization not found"));

		return modelMapper.map(org, OrganizationResponseDTO.class);
	}

	// changed added
	@Override
	public BigDecimal getAccountBalancebyOrgId(Long orgId) {
		Organization organization = organizationRepository.findById(orgId)
				.orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + orgId));

		// Check if organization is deleted
		if (organization.isDeleted()) {
			throw new IllegalStateException("Organization is deleted");
		}

		// Return account balance (default to ZERO if null)
		return organization.getAccountBalance() != null ? organization.getAccountBalance() : BigDecimal.ZERO;
	}

	@Override
	@Transactional(readOnly = true)
	public OrganizationResponseDTO getOrganizationWithDocuments(Long orgId) {
		// 1. Organization fetch karo
		Organization org = organizationRepository.findByOrgIdAndDeletedFalse(orgId)
				.orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + orgId));

		// 2. Documents fetch karo using repository
		List<OrganizationVerificationDocument> docs = orgVerificationDocRepository.findByOrganization_OrgId(orgId);

		// 3. Documents ko DTO mein convert karo
		List<VerificationDocumentDTO> docDTOs = docs.stream()
				.map(doc -> VerificationDocumentDTO.builder().docId(doc.getDocId()).filename(doc.getFilename())
						.cloudUrl(doc.getCloudUrl()).docType(doc.getDocType()).status(doc.getStatus())
						.uploadedAt(doc.getUploadedAt()).uploadedByUserId(doc.getUploadedByUserId()).build())
				.collect(Collectors.toList());

		// 4. Organization ko response DTO mein map karo
		OrganizationResponseDTO responseDTO = modelMapper.map(org, OrganizationResponseDTO.class);

		// 5. Documents set karo response mein
		responseDTO.setVerificationDocuments(docDTOs);

		return responseDTO;
	}
	// OrganizationService.java - Add this method
@Override
	@Transactional
	public void addBalance(Long orgId, BigDecimal amount) {
	    Organization org = organizationRepository.findById(orgId)
	        .orElseThrow(() -> new RuntimeException("Organization not found"));
	    
	    // Add amount to current balance
	    BigDecimal currentBalance = org.getAccountBalance();
	    org.setAccountBalance(currentBalance.add(amount));
	    
	    organizationRepository.save(org);
	    
	    // Log transaction
	    auditLogService.log(
	        "ADD_BALANCE",
	        "ORGANIZATION",
	        orgId,
	        null,
	        "Added balance: ₹" + amount,
	        "PAYMENT_SYSTEM"
	    );
	}

}
