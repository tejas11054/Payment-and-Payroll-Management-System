package com.paymentapp.serviceImpl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.paymentapp.dto.ChangePasswordDTO;
import com.paymentapp.dto.PaymentReceiptDTO;
import com.paymentapp.dto.VendorProfileDTO;
import com.paymentapp.dto.VendorRequestDTO;
import com.paymentapp.dto.VendorResponseDTO;
import com.paymentapp.entity.Organization;
import com.paymentapp.entity.PaymentReceipt;
import com.paymentapp.entity.PaymentRequest;
import com.paymentapp.entity.PaymentRequestApprovalHistory;
import com.paymentapp.entity.PaymentTransaction;
import com.paymentapp.entity.UploadBatch;
import com.paymentapp.entity.UploadBatchLine;
import com.paymentapp.entity.User;
import com.paymentapp.entity.Vendor;
import com.paymentapp.repository.OrganizationRepository;
import com.paymentapp.repository.PaymentReceiptRepository;
import com.paymentapp.repository.PaymentRequestApprovalHistoryRepository;
import com.paymentapp.repository.PaymentRequestRepository;
import com.paymentapp.repository.PaymentTransactionRepository;
import com.paymentapp.repository.RoleRepository;
import com.paymentapp.repository.UploadBatchLineRepository;
import com.paymentapp.repository.UploadBatchRepository;
import com.paymentapp.repository.UserRepository;
import com.paymentapp.repository.VendorRepository;
import com.paymentapp.service.AuditLogService;
import com.paymentapp.service.DocumentService;
import com.paymentapp.service.NotificationService;
import com.paymentapp.service.VendorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorServiceImpl implements VendorService {

	 private final VendorRepository vendorRepository;
	    private final OrganizationRepository organizationRepository;
	    private final RoleRepository roleRepository;
	    private final UploadBatchRepository uploadBatchRepository;
	    private final UploadBatchLineRepository uploadBatchLineRepository;
	    private final DocumentService documentService;
	    private final NotificationService notificationService;
	    private final AuditLogService auditLogService;
	    private final ModelMapper modelMapper;
	    private final PasswordEncoder passwordEncoder;
	    private final UserRepository userRepository;
	    private final PaymentRequestRepository paymentRequestRepository;
	    private final PaymentTransactionRepository paymentTransactionRepository;
	    private final PaymentRequestApprovalHistoryRepository paymentRequestApprovalHistoryRepository;
	    private final PaymentReceiptRepository paymentReceiptRepository;
	    private final AuditLogService auditLogService2;

	    private static final int MAX_EMAIL_LENGTH = 100;

	    @Override
	    @Transactional
	    public void addBalanceToVendor(Long vendorId, BigDecimal amount, User performingUser) {
	        Vendor vendor = vendorRepository.findById(vendorId)
	                .orElseThrow(() -> new RuntimeException("Vendor not found"));

	        if (vendor.getBalance() == null) {
	            vendor.setBalance(BigDecimal.ZERO);
	        }

	        vendor.setBalance(vendor.getBalance().add(amount));
	        vendorRepository.save(vendor);

	        auditLogService.log(
	            "ADD_VENDOR_BALANCE", "VENDOR",
	            vendorId,
	            performingUser.getUserId(),
	            performingUser.getEmail(),
	            performingUser.getRoles().stream()
	                .findFirst()
	                .map(r -> r.getRoleName())
	                .orElse("UNKNOWN")
	        );
	    }


	    @Override
	    @Transactional
	    public PaymentRequest initiateVendorPayment(Long orgId, Long vendorId, BigDecimal amount, String invoiceRef, User requestedBy) {
	        Organization org = organizationRepository.findById(orgId)
	                .orElseThrow(() -> new RuntimeException("Organization not found"));
	        Vendor vendor = vendorRepository.findById(vendorId)
	                .orElseThrow(() -> new RuntimeException("Vendor not found"));

	        PaymentRequest request = new PaymentRequest();
	        request.setAmount(amount);
	        request.setInvoiceReference(invoiceRef);
	        request.setStatus("PENDING");
	        request.setOrganization(org);
	        request.setVendor(vendor);
	        request.setRequestedBy(requestedBy);
	        request.setCreatedAt(Instant.now());
	        
	        auditLogService.log(
	        	    "INITIATE_VENDOR_PAYMENT", "VENDOR",
	        	    vendorId,
	        	    requestedBy.getUserId(),
	        	    requestedBy.getEmail(),
	        	    requestedBy.getRoles().stream().findFirst().map(r -> r.getRoleName()).orElse("UNKNOWN")
	        	);

	        return paymentRequestRepository.save(request);
	    }

	    @Override
	    @Transactional
	    public String processVendorPaymentRequest(Long paymentId, String action, String comment, User bankAdmin) {
	        PaymentRequest request = paymentRequestRepository.findById(paymentId)
	                .orElseThrow(() -> new RuntimeException("Payment request not found"));

	        if (!"PENDING".equals(request.getStatus())) {
	            return "Request is already processed.";
	        }

	        PaymentRequestApprovalHistory history = PaymentRequestApprovalHistory.builder()
	                .actedBy(bankAdmin)
	                .paymentRequest(request)
	                .action(action)
	                .comment(comment)
	                .build();
	        paymentRequestApprovalHistoryRepository.save(history);

	        if ("REJECTED".equalsIgnoreCase(action)) {
	            request.setStatus("REJECTED");
	            request.setProcessedAt(Instant.now());
	            request.setApprovedBy(bankAdmin);
	            paymentRequestRepository.save(request);
	            return "Payment request rejected.";
	        }

	        Organization org = request.getOrganization();
	        Vendor vendor = request.getVendor();

	        if (org.getAccountBalance().compareTo(request.getAmount()) < 0) {
	            throw new RuntimeException("Insufficient balance in organization account");
	        }

	        org.setAccountBalance(org.getAccountBalance().subtract(request.getAmount()));
	        vendor.setBalance(vendor.getBalance().add(request.getAmount()));
	        organizationRepository.save(org);
	        vendorRepository.save(vendor);

	        PaymentTransaction transaction = new PaymentTransaction();
	        transaction.setRelatedType("VENDOR");
	        transaction.setRelatedId(request.getPaymentId());
	        transaction.setAmount(request.getAmount());
	        transaction.setStatus("SUCCESS");
	        transaction.setProcessedBy(bankAdmin);
	        transaction.setOrganization(org);
	        transaction.setExecutedAt(Instant.now());
	        paymentTransactionRepository.save(transaction);

	        PaymentReceipt receipt = new PaymentReceipt();
	        receipt.setPaymentRequest(request);
	        receipt.setAmount(request.getAmount());
	        receipt.setCreatedAt(Instant.now());
	        receipt.setOrganization(org);
	        receipt.setVendor(vendor);
	        paymentReceiptRepository.save(receipt);

	        request.setStatus("PAID");
	        request.setProcessedAt(Instant.now());
	        request.setApprovedBy(bankAdmin);
	        paymentRequestRepository.save(request);
	        
	        auditLogService.log(
	        	    "PROCESS_VENDOR_PAYMENT_" + action.toUpperCase(), "VENDOR",
	        	    request.getVendor().getVendorId(),
	        	    bankAdmin.getUserId(),
	        	    bankAdmin.getEmail(),
	        	    bankAdmin.getRoles().stream().findFirst().map(r -> r.getRoleName()).orElse("UNKNOWN")
	        	);


	        return "Payment approved and processed successfully.";
	    }

    @Override
    @Transactional
    public VendorResponseDTO createVendor(Long orgId, VendorRequestDTO dto, MultipartFile documentFile, User performingUser) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        String email = dto.getContactEmail().trim().toLowerCase();
        if (email.length() > MAX_EMAIL_LENGTH) {
            throw new RuntimeException("Email too long");
        }
        if (vendorRepository.existsByContactEmailAndOrganizationAndDeletedFalse(email, organization)) {
            throw new RuntimeException("Vendor with this email already exists in this organization");
        }

        String pwd = generatePassword(organization.getOrgName(), dto.getName());

        Vendor vendor = new Vendor();
        vendor.setName(dto.getName());
        vendor.setVendorType(dto.getVendorType());
        vendor.setBankName(dto.getBankName());
        vendor.setBankAccountNo(dto.getBankAccountNo());
        vendor.setIfscCode(dto.getIfscCode());
        vendor.setContactEmail(email);
        vendor.setPhone(dto.getPhone());
        vendor.setOrganization(organization);
        vendor = vendorRepository.save(vendor);
        
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(pwd));
        user.setOrganization(organization);
        user.setStatus("ACTIVE");
        user.setDeleted(false);
        user.setRoles(Set.of(roleRepository.findByRoleName("ROLE_VENDOR").orElseThrow(() -> new RuntimeException("Vendor role not found"))));
        user = userRepository.save(user);
        
        vendor.setUser(user);
        

        if (documentFile != null && !documentFile.isEmpty()) {
            try {
                documentService.uploadAndSaveDocument(documentFile, "VENDOR", vendor.getVendorId(),
                        "VENDOR_DOCUMENT", performingUser.getUserId(), organization);
            } catch (Exception e) {
                throw new RuntimeException("Document upload failed for vendorId " + vendor.getVendorId() + ": " + e.getMessage(), e);
            }
        }

        notificationService.sendEmail(email, "Vendor Account Created",
                String.format(
                        "Dear %s,\n\nYour vendor account has been created successfully.\nYour default password is: %s\nPlease change your password after login.\n\nBest,\nPaymentApp Team",
                        dto.getName(), pwd));

        auditLogService.log("CREATE_VENDOR", "VENDOR",
                vendor.getVendorId(), 
                performingUser.getUserId(),
                performingUser.getEmail(),
                performingUser.getRoles().stream().findFirst().map(r -> r.getRoleName()).orElse("UNKNOWN"));

        VendorResponseDTO resp = modelMapper.map(vendor, VendorResponseDTO.class);
        resp.setOrganizationId(organization.getOrgId());
        return resp;
    }

    @Override
    @Transactional
    public List<VendorResponseDTO> createVendorsBulk(Long orgId, InputStream fileInputStream, String fileName,
                                                     String fileUrl, MultipartFile documentFile, User performingUser) throws IOException {
        if (fileName == null || fileName.isBlank()) {
            throw new RuntimeException("File name must be provided");
        }
        BufferedInputStream bis = new BufferedInputStream(fileInputStream);

        List<VendorRequestDTO> dtos;
        UserDataImportService importService = new UserDataImportService();
        if (fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls")) {
            dtos = importService.parseVendorExcel(bis);
        } else if (fileName.toLowerCase().endsWith(".csv")) {
            dtos = importService.parseVendorCsv(bis);
        } else {
            throw new RuntimeException("Unsupported file type: " + fileName);
        }

        if (dtos.isEmpty()) {
            throw new RuntimeException("No valid vendor records in file");
        }

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        UploadBatch batch = new UploadBatch();
        batch.setUploadedBy(performingUser.getUserId());
        batch.setFileUrl(fileUrl != null && !fileUrl.isBlank() ? fileUrl : "DirectUpload");
        batch.setOrganization(organization);
        batch.setProcessedCount(0);
        batch.setRecordCount(dtos.size());
        batch.setStatus("PENDING");
        batch.setEntityType("VENDOR");

        int rowNo = 1;
        for (VendorRequestDTO dto : dtos) {
            String rawData = dto.getContactEmail() != null ? dto.getContactEmail() : "";
            UploadBatchLine line = UploadBatchLine.builder()
                    .rowNumber(rowNo++)
                    .rawData(rawData)
                    .status("PENDING")
                    .message(null)
                    .entityType("VENDOR")
                    .uploadBatch(batch)
                    .build();
            batch.addBatchLine(line);
        }
        batch = uploadBatchRepository.save(batch);

        List<VendorResponseDTO> createdList = new ArrayList<>();
        int processedCount = 0;

        for (int i = 0; i < dtos.size(); i++) {
            VendorRequestDTO dto = dtos.get(i);
            UploadBatchLine line = batch.getLines().get(i);

            if (dto.getContactEmail() == null || dto.getContactEmail().isBlank()) {
                line.setStatus("FAILED");
                line.setMessage("Contact email missing");
                uploadBatchLineRepository.save(line);
                continue;
            }
            String normalizedEmail = dto.getContactEmail().trim().toLowerCase();

            if (normalizedEmail.length() > MAX_EMAIL_LENGTH) {
                line.setStatus("FAILED");
                line.setMessage("Email too long");
                uploadBatchLineRepository.save(line);
                continue;
            }

            if (vendorRepository.existsByContactEmailAndOrganizationAndDeletedFalse(normalizedEmail, organization)) {
                line.setStatus("FAILED");
                line.setMessage("Duplicate vendor email in organization");
                uploadBatchLineRepository.save(line);
                continue;
            }

            try {
                String pwd = generatePassword(organization.getOrgName(), dto.getName());
                BigDecimal default_balance= new BigDecimal("1000.000");
                Vendor vendor = new Vendor();
                vendor.setName(dto.getName());
                vendor.setVendorType(dto.getVendorType());
                vendor.setBankName(dto.getBankName());
                vendor.setBankAccountNo(dto.getBankAccountNo());
                vendor.setIfscCode(dto.getIfscCode());
                vendor.setContactEmail(normalizedEmail);
                vendor.setPhone(dto.getPhone());
                vendor.setBalance(default_balance);
                vendor.setOrganization(organization);
                vendor = vendorRepository.save(vendor);

                line.setEntityId(vendor.getVendorId());
                line.setStatus("SUCCESS");
                line.setMessage("Created");
                uploadBatchLineRepository.save(line);

                if (dto.getFileUrl() != null && !dto.getFileUrl().isBlank()) {
                    try {
                        documentService.uploadAndSaveDocumentFromUrl(
                                dto.getFileUrl(),
                                "VENDOR",
                                vendor.getVendorId(),
                                "BULK_VENDOR_DOC",
                                performingUser.getUserId(),
                                organization
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (documentFile != null && !documentFile.isEmpty()) {
                    try {
                        documentService.uploadAndSaveDocument(
                                documentFile,
                                "VENDOR",
                                vendor.getVendorId(),
                                "BULK_VENDOR_DOC",
                                performingUser.getUserId(),
                                organization
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                createdList.add(modelMapper.map(vendor, VendorResponseDTO.class));
                processedCount++;

                notificationService.sendEmail(normalizedEmail, "Vendor Account Created",
                        String.format(
                                "Dear %s,\n\nYour vendor account has been created successfully.\nYour default password is: %s\nPlease change your password after login.\n\nBest,\nPaymentApp Team",
                                dto.getName(), pwd));

                auditLogService.log("CREATE_VENDOR", "VENDOR",
                        vendor.getVendorId(),
                        performingUser.getUserId(),
                        performingUser.getEmail(),
                        performingUser.getRoles().stream().findFirst().map(r -> r.getRoleName()).orElse("UNKNOWN"));

            } catch (Exception ex) {
                ex.printStackTrace();
                line.setStatus("FAILED");
                line.setMessage("Error: " + ex.getMessage());
                uploadBatchLineRepository.save(line);
            }
        }

        batch.setProcessedCount(processedCount);
        batch.setStatus(processedCount == dtos.size() ? "COMPLETED" : "PARTIAL");
        uploadBatchRepository.save(batch);

        return createdList;
    }

    @Override
    public List<VendorResponseDTO> getVendorsByOrganization(Long orgId) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        List<Vendor> vendors = vendorRepository.findByOrganizationAndDeletedFalse(org);
        List<VendorResponseDTO> list = new ArrayList<>();
        for (Vendor v : vendors) {
            VendorResponseDTO dto = modelMapper.map(v, VendorResponseDTO.class);
            dto.setOrganizationId(org.getOrgId());
            list.add(dto);
        }
        return list;
    }

    @Override
    public VendorResponseDTO getVendorById(Long vendorId) {
        Vendor vendor = vendorRepository.findByVendorIdAndDeletedFalse(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        VendorResponseDTO dto = modelMapper.map(vendor, VendorResponseDTO.class);
        dto.setOrganizationId(vendor.getOrganization().getOrgId());
        return dto;
    }

    @Override
    @Transactional
    public VendorResponseDTO updateVendor(Long vendorId, VendorRequestDTO dto, User performingUser) {
        Vendor vendor = vendorRepository.findByVendorIdAndDeletedFalse(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        if (dto.getContactEmail() != null) {
            String newEmail = dto.getContactEmail().trim().toLowerCase();
            if (!vendor.getContactEmail().equalsIgnoreCase(newEmail)) {
                boolean exists = vendorRepository.existsByContactEmailAndOrganizationAndVendorIdNotAndDeletedFalse(
                        newEmail, vendor.getOrganization(), vendorId);
                if (exists) {
                    throw new RuntimeException("Email already used by another vendor in organization");
                }
                vendor.setContactEmail(newEmail);
            }
        }
        if (dto.getName() != null) {
            vendor.setName(dto.getName());
        }
        if (dto.getVendorType() != null) {
            vendor.setVendorType(dto.getVendorType());
        }
        if (dto.getBankName() != null) {
            vendor.setBankName(dto.getBankName()); 
        }
        if (dto.getBankAccountNo() != null) {
            vendor.setBankAccountNo(dto.getBankAccountNo());
        }
        if (dto.getPhone() != null) {
            vendor.setPhone(dto.getPhone());
        }
        if (dto.getIfscCode() != null) {
            vendor.setIfscCode(dto.getIfscCode());
        }

        vendor = vendorRepository.save(vendor);

        auditLogService.log("UPDATE_VENDOR", "VENDOR",
                vendor.getVendorId(), performingUser.getUserId(),
                performingUser.getEmail(),
                performingUser.getRoles().stream().findFirst().map(r -> r.getRoleName()).orElse("UNKNOWN"));

        VendorResponseDTO resp = modelMapper.map(vendor, VendorResponseDTO.class);
        resp.setOrganizationId(vendor.getOrganization().getOrgId());
        return resp;
    }

    @Override
    @Transactional
    public void deleteVendor(Long vendorId, User performingUser) {
        Vendor vendor = vendorRepository.findByVendorIdAndDeletedFalse(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        vendor.setDeleted(true);
        vendorRepository.save(vendor);
        
        User user = vendor.getUser();
        if(user != null) {
        	user.setDeleted(true);
        	user.setStatus("INACTIVE");
        	userRepository.save(user);
        }

        notificationService.sendEmail(vendor.getContactEmail(), "Vendor Deleted",
                String.format("Vendor %s has been deleted.", vendor.getName()));

        auditLogService.log("DELETE_VENDOR", "VENDOR",
                vendor.getVendorId(), performingUser.getUserId(),
                performingUser.getEmail(),
                performingUser.getRoles().stream().findFirst().map(r -> r.getRoleName()).orElse("UNKNOWN"));
    }

    private String generatePassword(String orgName, String entityName) {
        String orgPart = orgName.length() >= 2 ? orgName.substring(0, 2).toUpperCase() : orgName.toUpperCase();
        String entPart = entityName.length() >= 2 ? entityName.substring(0, 2).toUpperCase() : entityName.toUpperCase();

        StringBuilder sb = new StringBuilder();
        sb.append(orgPart);
        sb.append(entPart);
        sb.append("@");

        SecureRandom random = new SecureRandom();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
    @Override
    public VendorProfileDTO getVendorProfileByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Vendor vendor = vendorRepository.findByUserAndDeletedFalse(user)
                .orElseThrow(() -> new RuntimeException("Vendor profile not found"));
        
        return mapToVendorProfileDTO(vendor);
    }

    // ═══════════════════════════════════════════════════════════════════
    // UPDATE VENDOR OWN PROFILE
    // ═══════════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public VendorProfileDTO updateVendorOwnProfile(Long userId, VendorProfileDTO profileDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Vendor vendor = vendorRepository.findByUserAndDeletedFalse(user)
                .orElseThrow(() -> new RuntimeException("Vendor profile not found"));
        
        // Update only phone
        if (profileDto.getPhone() != null && !profileDto.getPhone().isBlank()) {
            vendor.setPhone(profileDto.getPhone());
        }
        
        // Bank details
        if (profileDto.getBankName() != null) {
            vendor.setBankName(profileDto.getBankName());
        }
        if (profileDto.getBankAccountNo() != null) {
            vendor.setBankAccountNo(profileDto.getBankAccountNo());
        }
        if (profileDto.getIfscCode() != null) {
            vendor.setIfscCode(profileDto.getIfscCode());
        }
        if (profileDto.getAccountHolderName() != null) {
            vendor.setName(profileDto.getAccountHolderName());
        }
        
        vendor = vendorRepository.save(vendor);
        
        auditLogService.log("UPDATE_OWN_PROFILE", "VENDOR",
                vendor.getVendorId(), userId, user.getEmail(), "VENDOR");
        
        return mapToVendorProfileDTO(vendor);
    }

    // ═══════════════════════════════════════════════════════════════════
    // GET MY RECEIPTS
    // ═══════════════════════════════════════════════════════════════════
    @Override
    public List<PaymentReceiptDTO> getMyReceipts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Vendor vendor = vendorRepository.findByUserAndDeletedFalse(user)
                .orElseThrow(() -> new RuntimeException("Vendor profile not found"));
        
        // ✅ MOST SIMPLE FIX
        List<PaymentReceipt> receipts = paymentReceiptRepository.findByVendorVendorId(vendor.getVendorId());
        
        return receipts.stream()
                .map(this::mapToPaymentReceiptDTO)
                .collect(Collectors.toList());
    }


    // ✅ HELPER METHOD - Takes PaymentReceipt as parameter
    private PaymentReceiptDTO mapToPaymentReceiptDTO(PaymentReceipt receipt) {
        return PaymentReceiptDTO.builder()
                .receiptId(receipt.getReceiptId())
                .paymentId(receipt.getPaymentRequest().getPaymentId())
                .amount(receipt.getAmount())
                .bankReference(receipt.getBankReference())
                .status(receipt.getStatus())
                .vendorId(receipt.getVendor().getVendorId())
                .vendorName(receipt.getVendor().getName())
                .orgId(receipt.getOrganization().getOrgId())
                .orgName(receipt.getOrganization().getOrgName())
                .createdAt(receipt.getCreatedAt())
                .build();
    }


    // ═══════════════════════════════════════════════════════════════════
    // GET RECEIPT DETAILS
    // ═══════════════════════════════════════════════════════════════════
    @Override
    public PaymentReceiptDTO getReceiptDetails(Long receiptId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Vendor vendor = vendorRepository.findByUserAndDeletedFalse(user)
                .orElseThrow(() -> new RuntimeException("Vendor profile not found"));
        
        PaymentReceipt receipt = paymentReceiptRepository.findById(receiptId)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));
        
        // Verify ownership
        if (!receipt.getVendor().getVendorId().equals(vendor.getVendorId())) {
            throw new RuntimeException("Access denied - Not your receipt");
        }
        
        return mapToPaymentReceiptDTO(receipt);
    }

    // ═══════════════════════════════════════════════════════════════════
    // DOWNLOAD RECEIPT
    // ═══════════════════════════════════════════════════════════════════
    @Override
    public byte[] downloadReceipt(Long receiptId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Vendor vendor = vendorRepository.findByUserAndDeletedFalse(user)
                .orElseThrow(() -> new RuntimeException("Vendor profile not found"));
        
        PaymentReceipt receipt = paymentReceiptRepository.findById(receiptId)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));
        
        // Verify ownership
        if (!receipt.getVendor().getVendorId().equals(vendor.getVendorId())) {
            throw new RuntimeException("Access denied - Not your receipt");
        }
        
        String receiptText = generateReceiptText(receipt);
        return receiptText.getBytes();
    }

    @Override
    @Transactional
    public void changeVendorPassword(Long userId, ChangePasswordDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Validate new password
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("New passwords do not match");
        }
        
        if (dto.getNewPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        
        auditLogService.log("CHANGE_PASSWORD", "USER",
                userId, userId, user.getEmail(), "VENDOR");
        
        // Send notification
        notificationService.sendEmail(user.getEmail(), "Password Changed",
                "Your password has been changed successfully. If you did not make this change, please contact support immediately.");
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════
    private VendorProfileDTO mapToVendorProfileDTO(Vendor vendor) {
        return VendorProfileDTO.builder()
                .vendorId(vendor.getVendorId())
                .name(vendor.getName())
                .contactEmail(vendor.getContactEmail())
                .phone(vendor.getPhone())
                .vendorType(vendor.getVendorType())
                .balance(vendor.getBalance() != null ? vendor.getBalance() : BigDecimal.ZERO)
                .bankName(vendor.getBankName())
                .bankAccountNo(vendor.getBankAccountNo())
                .ifscCode(vendor.getIfscCode())
                .accountHolderName(vendor.getName())
                .orgId(vendor.getOrganization().getOrgId())
                .orgName(vendor.getOrganization().getOrgName())
                .createdAt(vendor.getCreatedAt())
                .build();
    }


    private String generateReceiptText(PaymentReceipt receipt) {
        return String.format("""
            ═══════════════════════════════════════════════════════════
                               PAYMENT RECEIPT
            ═══════════════════════════════════════════════════════════
            
            Receipt ID      : %d
            Bank Reference  : %s
            Payment ID      : %d
            Date            : %s
            
            ───────────────────────────────────────────────────────────
            FROM ORGANIZATION
            ───────────────────────────────────────────────────────────
            Organization    : %s
            Org ID          : %d
            
            ───────────────────────────────────────────────────────────
            TO VENDOR
            ───────────────────────────────────────────────────────────
            Vendor Name     : %s
            Vendor ID       : %d
            
            ───────────────────────────────────────────────────────────
            PAYMENT DETAILS
            ───────────────────────────────────────────────────────────
            Amount Paid     : ₹ %s
            Status          : %s
            
            ═══════════════════════════════════════════════════════════
                        This is a computer-generated receipt
            ═══════════════════════════════════════════════════════════
            """,
            receipt.getReceiptId(),
            receipt.getBankReference(),
            receipt.getPaymentRequest().getPaymentId(),
            receipt.getCreatedAt(),
            receipt.getOrganization().getOrgName(),
            receipt.getOrganization().getOrgId(),
            receipt.getVendor().getName(),
            receipt.getVendor().getVendorId(),
            receipt.getAmount(),
            receipt.getStatus()
        );
    }

}
