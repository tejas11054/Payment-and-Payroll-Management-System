package com.paymentapp.serviceImpl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paymentapp.dto.PaymentApprovalDTO;
import com.paymentapp.dto.PaymentReceiptDTO;
import com.paymentapp.dto.PaymentRequestDTO;
import com.paymentapp.entity.Organization;
import com.paymentapp.entity.PaymentReceipt;
import com.paymentapp.entity.PaymentRequest;
import com.paymentapp.entity.PaymentRequestApprovalHistory;
import com.paymentapp.entity.PaymentTransaction;
import com.paymentapp.entity.User;
import com.paymentapp.entity.Vendor;
import com.paymentapp.repository.OrganizationRepository;
import com.paymentapp.repository.PaymentReceiptRepository;
import com.paymentapp.repository.PaymentRequestApprovalHistoryRepository;
import com.paymentapp.repository.PaymentRequestRepository;
import com.paymentapp.repository.PaymentTransactionRepository;
import com.paymentapp.repository.UserRepository;
import com.paymentapp.repository.VendorRepository;
import com.paymentapp.service.AuditLogService;
import com.paymentapp.service.NotificationService;
import com.paymentapp.service.PaymentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRequestRepository requestRepo;
    private final PaymentRequestApprovalHistoryRepository historyRepo;
    private final PaymentTransactionRepository txnRepo;
    private final PaymentReceiptRepository receiptRepo;
    private final VendorRepository vendorRepo;
    private final OrganizationRepository orgRepo;
    private final UserRepository userRepo;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    // ═══════════════════════════════════════════════════════════════════
    // CREATE PAYMENT REQUEST
    // ═══════════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public PaymentRequestDTO createRequest(PaymentRequestDTO dto) {
        // Validations
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (dto.getInvoiceReference() == null || dto.getInvoiceReference().trim().isEmpty()) {
            throw new IllegalArgumentException("Invoice reference is required");
        }

        // Fetch entities
        Organization org = orgRepo.findById(dto.getOrgId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        Vendor vendor = vendorRepo.findById(dto.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        User requester = userRepo.findById(dto.getRequestedById())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check balance
        if (org.getAccountBalance().compareTo(dto.getAmount()) < 0) {
            throw new RuntimeException("Insufficient organization balance");
        }

        // Create request
        PaymentRequest req = new PaymentRequest();
        req.setAmount(dto.getAmount());
        req.setInvoiceReference(dto.getInvoiceReference());
        req.setOrganization(org);
        req.setVendor(vendor);
        req.setRequestedBy(requester);
        req.setStatus("PENDING");

        req = requestRepo.save(req);

        // Audit log
        auditLogService.log(
            "CREATE_PAYMENT_REQUEST", 
            "PAYMENT_REQUEST",
            req.getPaymentId(),
            requester.getUserId(),
            requester.getEmail(),
            requester.getRoles().stream().findFirst().map(r -> r.getRoleName()).orElse("ORG_ADMIN")
        );

        return toDTO(req);
    }

    // ═══════════════════════════════════════════════════════════════════
    // GET PENDING REQUESTS
    // ═══════════════════════════════════════════════════════════════════
    @Override
    public List<PaymentRequestDTO> getPendingRequests() {
        return requestRepo.findByStatus("PENDING").stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════
    // GET REQUEST BY ID
    // ═══════════════════════════════════════════════════════════════════
    @Override
    public PaymentRequestDTO getRequestById(Long paymentId) {
        PaymentRequest req = requestRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment request not found"));
        return toDTO(req);
    }

    // ═══════════════════════════════════════════════════════════════════
    // GET REQUESTS BY ORG
    // ═══════════════════════════════════════════════════════════════════
    @Override
    public List<PaymentRequestDTO> getRequestsByOrg(Long orgId) {
        Organization org = orgRepo.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        return requestRepo.findByOrganization(org).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    
    @Override
    @Transactional
    public PaymentRequestDTO approveRequest(Long paymentId, PaymentApprovalDTO approvalDto, User approver) {
        // Fetch request
        PaymentRequest req = requestRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment request not found"));

        // ✅ CRITICAL: Check if already processed
        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new RuntimeException(
                String.format("Payment request already %s. Cannot process again!", req.getStatus())
            );
        }

        // Validate approver
        if (approver == null) {
            throw new RuntimeException("Approver is required");
        }

        // Validate balance
        Organization org = req.getOrganization();
        BigDecimal amount = req.getAmount();
        
        if (org.getAccountBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // ✅ IMPORTANT: Update status FIRST before processing payment
        req.setStatus("APPROVED");
        req.setApprovedBy(approver);
        req.setProcessedAt(Instant.now());
        req = requestRepo.save(req); // Save immediately!

        // Save history
        PaymentRequestApprovalHistory history = PaymentRequestApprovalHistory.builder()
                .paymentRequest(req)
                .action("APPROVED")
                .comment(approvalDto != null ? approvalDto.getComment() : "Approved")
                .actedBy(approver)
                .build();
        historyRepo.save(history);

        // Audit log
        auditLogService.log(
            "APPROVE_PAYMENT",
            "PAYMENT_REQUEST",
            req.getPaymentId(),
            approver.getUserId(),
            approver.getEmail(),
            approver.getRoles().stream().findFirst().map(r -> r.getRoleName()).orElse("BANK_ADMIN")
        );

        // ✅ NOW process payment (status already APPROVED, can't be called again)
        processPayment(req, approver);

        // Send notifications
        sendPaymentApprovalNotifications(req, approver);

        return toDTO(req);
    }
    

    // ═══════════════════════════════════════════════════════════════════
    // REJECT REQUEST - WITH NOTIFICATIONS
    // ═══════════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public PaymentRequestDTO rejectRequest(Long paymentId, PaymentApprovalDTO approvalDto, User approver) {
        // Fetch request
        PaymentRequest req = requestRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment request not found"));

        // Validate status
        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new RuntimeException("Request already processed");
        }

        // Validate approver
        if (approver == null) {
            throw new RuntimeException("Approver is required");
        }

        // Validate reason
        if (approvalDto == null || approvalDto.getComment() == null || approvalDto.getComment().trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }

        // Update request
        req.setStatus("REJECTED");
        req.setApprovedBy(approver);
        req.setProcessedAt(Instant.now());
        requestRepo.save(req);

        // Save history
        PaymentRequestApprovalHistory history = PaymentRequestApprovalHistory.builder()
                .paymentRequest(req)
                .action("REJECTED")
                .comment(approvalDto.getComment())
                .actedBy(approver)
                .build();
        historyRepo.save(history);

        // Audit log
        auditLogService.log(
            "REJECT_PAYMENT",
            "PAYMENT_REQUEST",
            req.getPaymentId(),
            approver.getUserId(),
            approver.getEmail(),
            approver.getRoles().stream().findFirst().map(r -> r.getRoleName()).orElse("BANK_ADMIN")
        );

        // ✅ SEND REJECTION NOTIFICATIONS
        sendPaymentRejectionNotifications(req, approver, approvalDto.getComment());

        return toDTO(req);
    }

    // ═══════════════════════════════════════════════════════════════════
    // ✅ NEW: SEND APPROVAL NOTIFICATIONS
    // ═══════════════════════════════════════════════════════════════════
    private void sendPaymentApprovalNotifications(PaymentRequest req, User approver) {
        Organization org = req.getOrganization();
        Vendor vendor = req.getVendor();
        BigDecimal amount = req.getAmount();
        
        try {
            // ✅ 1. Notify Organization Admin (requester)
            User orgAdmin = req.getRequestedBy();
            notificationService.createInAppNotification(
                orgAdmin,
                "💰 Payment Request Approved",
                String.format("Your payment request of ₹%s to vendor %s has been approved. Amount deducted from organization account.",
                    amount, vendor.getName()),
                req.getPaymentId(),
                "PAYMENT_REQUEST",
                "HIGH"
            );
            
            // ✅ 2. Notify all Organization Admins
            List<User> orgAdmins = userRepo.findByOrganizationAndRolesRoleNameAndDeletedFalse(org, "ORG_ADMIN");
            for (User admin : orgAdmins) {
                if (!admin.getUserId().equals(orgAdmin.getUserId())) {
                    notificationService.createInAppNotification(
                        admin,
                        "💸 Payment Processed",
                        String.format("Payment of ₹%s to vendor %s approved. New balance: ₹%s",
                            amount, vendor.getName(), org.getAccountBalance()),
                        req.getPaymentId(),
                        "PAYMENT_REQUEST",
                        "MEDIUM"
                    );
                }
            }
            
            // ✅ 3. Notify Vendor (if vendor has user account)
            // Assuming vendor might have a linked user account
            if (vendor.getUser() != null) {
                notificationService.createInAppNotification(
                    vendor.getUser(),
                    "✅ Payment Received",
                    String.format("Payment of ₹%s received from %s. New balance: ₹%s",
                        amount, org.getOrgName(), vendor.getBalance()),
                    req.getPaymentId(),
                    "PAYMENT_RECEIPT",
                    "HIGH"
                );
            }
            
            System.out.println("✅ Payment approval notifications sent successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Error sending payment approval notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // ✅ NEW: SEND REJECTION NOTIFICATIONS
    // ═══════════════════════════════════════════════════════════════════
    private void sendPaymentRejectionNotifications(PaymentRequest req, User approver, String reason) {
        Organization org = req.getOrganization();
        Vendor vendor = req.getVendor();
        BigDecimal amount = req.getAmount();
        
        try {
            // ✅ Notify Organization Admin (requester)
            User orgAdmin = req.getRequestedBy();
            notificationService.createInAppNotification(
                orgAdmin,
                "❌ Payment Request Rejected",
                String.format("Your payment request of ₹%s to vendor %s has been rejected. Reason: %s",
                    amount, vendor.getName(), reason),
                req.getPaymentId(),
                "PAYMENT_REQUEST",
                "HIGH"
            );
            
            // ✅ Notify all Organization Admins
            List<User> orgAdmins = userRepo.findByOrganizationAndRolesRoleNameAndDeletedFalse(org, "ORG_ADMIN");
            for (User admin : orgAdmins) {
                if (!admin.getUserId().equals(orgAdmin.getUserId())) {
                    notificationService.createInAppNotification(
                        admin,
                        "❌ Payment Rejected",
                        String.format("Payment request of ₹%s to vendor %s rejected. Reason: %s",
                            amount, vendor.getName(), reason),
                        req.getPaymentId(),
                        "PAYMENT_REQUEST",
                        "MEDIUM"
                    );
                }
            }
            
            System.out.println("✅ Payment rejection notifications sent successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Error sending payment rejection notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // ═══════════════════════════════════════════════════════════════════
    // GET ALL REQUESTS
    // ═══════════════════════════════════════════════════════════════════
    @Override
    public List<PaymentRequestDTO> getAllRequests() {
        return requestRepo.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════
    // PROCESS PAYMENT (DEDUCT & ADD BALANCE)
    // ═══════════════════════════════════════════════════════════════════
    private void processPayment(PaymentRequest req, User approver) {
        Organization org = req.getOrganization();
        Vendor vendor = req.getVendor();
        BigDecimal amount = req.getAmount();

        // Deduct from org
        org.setAccountBalance(org.getAccountBalance().subtract(amount));
        orgRepo.save(org);

        // Add to vendor
        vendor.setBalance(vendor.getBalance().add(amount));
        vendorRepo.save(vendor);

        // Create transaction
        String bankRef = "BANK-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        
        PaymentTransaction txn = new PaymentTransaction();
        txn.setRelatedType("VENDOR");
        txn.setRelatedId(req.getPaymentId());
        txn.setAmount(amount);
        txn.setStatus("SUCCESS");
        txn.setBankReference(bankRef);
        txn.setProcessedBy(approver);
        txn.setOrganization(org);
        txnRepo.save(txn);

        // Generate receipt
        generateReceipt(req, txn, approver);
    }

    // ═══════════════════════════════════════════════════════════════════
    // GENERATE RECEIPT
    // ═══════════════════════════════════════════════════════════════════
    private void generateReceipt(PaymentRequest req, PaymentTransaction txn, User approver) {
        PaymentReceipt receipt = PaymentReceipt.builder()
                .paymentRequest(req)
                .amount(req.getAmount())
                .bankReference(txn.getBankReference())
                .status("PAID")
                .vendor(req.getVendor())
                .organization(req.getOrganization())
                .build();
        receiptRepo.save(receipt);

        // Audit log
        auditLogService.log(
            "GENERATE_RECEIPT",
            "PAYMENT_RECEIPT",
            receipt.getReceiptId(),
            approver.getUserId(),
            approver.getEmail(),
            approver.getRoles().stream().findFirst().map(r -> r.getRoleName()).orElse("SYSTEM")
        );
    }

    // ═══════════════════════════════════════════════════════════════════
    // GET RECEIPT BY PAYMENT ID
    // ═══════════════════════════════════════════════════════════════════
    @Override
    public PaymentReceiptDTO getReceipt(Long paymentId) {
        PaymentRequest req = requestRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment request not found"));
        
        PaymentReceipt receipt = receiptRepo.findByPaymentRequest(req)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));

        return toReceiptDTO(receipt);
    }

    // ═══════════════════════════════════════════════════════════════════
    // GET RECEIPTS BY ORG
    // ═══════════════════════════════════════════════════════════════════
    @Override
    public List<PaymentReceiptDTO> getReceiptsByOrg(Long orgId) {
        return receiptRepo.findByOrganizationOrgId(orgId).stream()
                .map(this::toReceiptDTO)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════
    // MAP TO DTO
    // ═══════════════════════════════════════════════════════════════════
 // In PaymentServiceImpl - Update toDTO method

    private PaymentRequestDTO toDTO(PaymentRequest req) {
        PaymentRequestDTO dto = new PaymentRequestDTO();
        dto.setPaymentId(req.getPaymentId());
        dto.setAmount(req.getAmount());
        dto.setInvoiceReference(req.getInvoiceReference());
        dto.setStatus(req.getStatus());
        dto.setOrgId(req.getOrganization().getOrgId());
        dto.setVendorId(req.getVendor().getVendorId());
        dto.setRequestedById(req.getRequestedBy().getUserId());
        
        // ✅ ADD THESE LINES
        dto.setOrgName(req.getOrganization().getOrgName());
        dto.setVendorName(req.getVendor().getName());
        dto.setBankAccountNo(req.getVendor().getBankAccountNo());
        dto.setIfscCode(req.getVendor().getIfscCode());
        dto.setBankName(req.getVendor().getBankName());
        
        if (req.getApprovedBy() != null) {
            dto.setApprovedById(req.getApprovedBy().getUserId());
        }
        
        dto.setCreatedAt(req.getCreatedAt());
        dto.setProcessedAt(req.getProcessedAt());
        return dto;
    }
    

    // ═══════════════════════════════════════════════════════════════════
    // MAP RECEIPT TO DTO
    // ═══════════════════════════════════════════════════════════════════
    private PaymentReceiptDTO toReceiptDTO(PaymentReceipt receipt) {
        PaymentReceiptDTO dto = new PaymentReceiptDTO();
        dto.setReceiptId(receipt.getReceiptId());
        dto.setPaymentId(receipt.getPaymentRequest().getPaymentId());
        dto.setAmount(receipt.getAmount());
        dto.setBankReference(receipt.getBankReference());
        dto.setStatus(receipt.getStatus());
        dto.setVendorId(receipt.getVendor().getVendorId());
        dto.setVendorName(receipt.getVendor().getName());
        dto.setOrgId(receipt.getOrganization().getOrgId());
        dto.setOrgName(receipt.getOrganization().getOrgName());
        dto.setCreatedAt(receipt.getCreatedAt());
        return dto;
    }
}
