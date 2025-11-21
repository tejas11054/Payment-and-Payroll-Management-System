	package com.paymentapp.serviceImpl;
	
	import java.math.BigDecimal;
	import java.util.ArrayList;
	import java.util.List;
	import java.util.stream.Collectors;
	
	import org.springframework.security.core.Authentication;
	import org.springframework.security.core.context.SecurityContextHolder;
	import org.springframework.stereotype.Service;
	import org.springframework.transaction.annotation.Transactional;
	
	import com.paymentapp.dto.SalaryDisbursalApprovalRequestDTO;
	import com.paymentapp.dto.SalaryDisbursalLineDTO;
	import com.paymentapp.dto.SalaryDisbursalRequestDTO;
	import com.paymentapp.entity.*;
	import com.paymentapp.repository.*;
	import com.paymentapp.service.AuditLogService;
	import com.paymentapp.service.NotificationService;
	import com.paymentapp.service.SalaryDisbursalApprovalService;
	
	import lombok.RequiredArgsConstructor;
	
	@Service
	@RequiredArgsConstructor
	public class SalaryDisbursalApprovalServiceImpl implements SalaryDisbursalApprovalService {
	
	    // ================= REPOSITORIES =================
	    private final SalaryDisbursalRequestRepository disbursalRequestRepo;
	    private final OrganizationRepository organizationRepository;
	    private final EmployeeRepository employeeRepository;
	    private final OrgAdminRepository orgAdminRepository;
	    private final SalaryDisbursalApprovalHistoryRepository approvalHistoryRepo;
	    private final SalarySlipRepository salarySlipRepository;
	    private final UserRepository userRepository;
	    
	    // ================= SERVICES =================
	    private final AuditLogService auditLogService;
	    private final NotificationService notificationService;
	
	    // ================= GET PENDING REQUESTS =================
	    @Override
	    @Transactional(readOnly = true)
	    public List<SalaryDisbursalRequestDTO> getPendingRequests() {
	        System.out.println("📋 Fetching pending salary disbursal requests...");
	        
	        List<SalaryDisbursalRequest> requests = disbursalRequestRepo.findByStatus("PENDING");
	        
	        System.out.println("✅ Found " + requests.size() + " pending requests");
	        
	        return requests.stream()
	                .map(this::mapToDTO)
	                .collect(Collectors.toList());
	    }
	
	    // ================= GET REQUEST DETAILS =================
	    @Override
	    @Transactional(readOnly = true)
	    public SalaryDisbursalRequestDTO getRequestDetails(Long disbursalId) {
	        System.out.println("🔍 Fetching details for disbursal ID: " + disbursalId);
	        
	        SalaryDisbursalRequest request = disbursalRequestRepo.findById(disbursalId)
	                .orElseThrow(() -> new RuntimeException("Salary disbursal request not found with ID: " + disbursalId));
	        
	        SalaryDisbursalRequestDTO dto = mapToDTO(request);
	        
	        System.out.println("✅ Retrieved request for " + dto.getOrgName() + 
	                         " | Period: " + dto.getPeriod() + 
	                         " | Amount: ₹" + dto.getTotalAmount());
	        
	        return dto;
	    }
	
	    // ================= PROCESS APPROVAL =================
	    @Override
	    @Transactional
	    public void processApproval(SalaryDisbursalApprovalRequestDTO approvalRequest) {
	        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	        String username = auth.getName();
	
	        User actingUser = userRepository.findByEmail(username)
	                .orElseThrow(() -> new RuntimeException("Acting user not found"));
	
	        SalaryDisbursalRequest request = disbursalRequestRepo.findById(approvalRequest.getDisbursalRequestId())
	                .orElseThrow(() -> new RuntimeException("Disbursal request not found with ID: " 
	                        + approvalRequest.getDisbursalRequestId()));
	
	        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
	            throw new RuntimeException("Request already processed with status: " + request.getStatus());
	        }
	
	        auditLogService.log(
	                "PROCESS_APPROVAL_STARTED",
	                "SalaryDisbursalRequest",
	                request.getDisbursalId(),
	                actingUser.getUserId(),
	                actingUser.getEmail(),
	                getFirstRole(actingUser)
	        );
	        
	        System.out.println("🔄 Processing salary disbursal " + approvalRequest.getAction() + 
	                         " for Organization: " + request.getOrganization().getOrgName() + 
	                         " | Period: " + request.getPeriod() + 
	                         " | Amount: ₹" + request.getTotalAmount());
	
	        String action = approvalRequest.getAction().toUpperCase();
	
	        switch (action) {
	            case "APPROVE":
	                approveRequest(request, actingUser, approvalRequest.getComment());
	                auditLogService.log(
	                        "APPROVED_SALARY_DISBURSAL",
	                        "SalaryDisbursalRequest",
	                        request.getDisbursalId(),
	                        actingUser.getUserId(),
	                        actingUser.getEmail(),
	                        getFirstRole(actingUser)
	                );
	                break;
	
	            case "REJECT":
	                rejectRequest(request, actingUser, approvalRequest.getComment());
	                auditLogService.log(
	                        "REJECTED_SALARY_DISBURSAL",
	                        "SalaryDisbursalRequest",
	                        request.getDisbursalId(),
	                        actingUser.getUserId(),
	                        actingUser.getEmail(),
	                        getFirstRole(actingUser)
	                );
	                break;
	
	            default:
	                auditLogService.log(
	                        "INVALID_ACTION_ATTEMPTED",
	                        "SalaryDisbursalRequest",
	                        request.getDisbursalId(),
	                        actingUser.getUserId(),
	                        actingUser.getEmail(),
	                        getFirstRole(actingUser)
	                );
	                throw new RuntimeException("Invalid action: " + approvalRequest.getAction() + 
	                                         ". Valid actions are: APPROVE, REJECT");
	        }
	    }
	
	    // ================= APPROVE REQUEST =================
	    private void approveRequest(SalaryDisbursalRequest request, User actingUser, String comment) {
	        Organization org = request.getOrganization();
	        BigDecimal totalAmount = request.getTotalAmount();
	
	        System.out.println("💰 Organization: " + org.getOrgName());
	        System.out.println("💰 Current Balance: ₹" + org.getAccountBalance());
	        System.out.println("💰 Required Amount: ₹" + totalAmount);
	
	        if (org.getAccountBalance() == null || org.getAccountBalance().compareTo(totalAmount) < 0) {
	            BigDecimal currentBalance = org.getAccountBalance() != null ? 
	                                       org.getAccountBalance() : BigDecimal.ZERO;
	            BigDecimal shortfall = totalAmount.subtract(currentBalance);
	            
	            String errorMsg = String.format(
	                "Insufficient funds: Organization '%s' has ₹%s but requires ₹%s. Shortfall: ₹%s",
	                org.getOrgName(),
	                currentBalance,
	                totalAmount,
	                shortfall
	            );
	            
	            System.err.println("❌ " + errorMsg);
	            
	            auditLogService.log(
	                    "APPROVAL_FAILED_INSUFFICIENT_FUNDS",
	                    "Organization",
	                    org.getOrgId(),
	                    actingUser.getUserId(),
	                    actingUser.getEmail(),
	                    getFirstRole(actingUser)
	            );
	            
	            throw new RuntimeException(errorMsg);
	        }
	
	        BigDecimal newOrgBalance = org.getAccountBalance().subtract(totalAmount);
	        org.setAccountBalance(newOrgBalance);
	        organizationRepository.save(org);
	        
	        System.out.println("💰 New Organization Balance: ₹" + newOrgBalance);
	        
	        auditLogService.log(
	                "ORGANIZATION_BALANCE_UPDATED",
	                "Organization",
	                org.getOrgId(),
	                actingUser.getUserId(),
	                actingUser.getEmail(),
	                getFirstRole(actingUser)
	        );
	
	        int processedCount = 0;
	        for (SalaryDisbursalLine line : request.getLines()) {
	            User user = null;
	            Employee emp = line.getEmployee();
	            OrgAdmin orgAdmin = line.getOrgAdmin();
	            String recipientName = "";
	            String recipientType = "";
	
	            if (emp != null) {
	                user = emp.getUser();
	                recipientName = emp.getEmpName();
	                recipientType = "Employee";
	            } else if (orgAdmin != null) {
	                user = orgAdmin.getUser();
	                recipientName = orgAdmin.getName();
	                recipientType = "Org Admin";
	            }
	
	            if (user == null) {
	                String error = "No user found for " + recipientType + " in line ID: " + line.getLineId();
	                System.err.println("❌ " + error);
	                
	                auditLogService.log(
	                        "USER_MISSING_FOR_LINE",
	                        "SalaryDisbursalLine",
	                        line.getLineId(),
	                        actingUser.getUserId(),
	                        actingUser.getEmail(),
	                        getFirstRole(actingUser)
	                );
	                throw new RuntimeException(error);
	            }
	//
	//            BigDecimal currentBalance;
	//            BigDecimal newBalance;
	//            
	//            if (emp != null) {
	//                currentBalance = emp.getBalance() != null ? emp.getBalance() : BigDecimal.ZERO;
	//                newBalance = currentBalance.add(line.getNetAmount());
	//                emp.setBalance(newBalance);
	//                employeeRepository.save(emp);
	//            } else {
	//                currentBalance = orgAdmin.getBalance() != null ? orgAdmin.getBalance() : BigDecimal.ZERO;
	//                newBalance = currentBalance.add(line.getNetAmount());
	//                orgAdmin.setBalance(newBalance);
	//                orgAdminRepository.save(orgAdmin);
	//            }
	
	            line.setStatus("PAID");
	            
	            processedCount++;
	//            System.out.println("✅ Paid " + recipientType + ": " + recipientName + 
	//                             " | ₹" + line.getNetAmount() + 
	//                             " | New Balance: ₹" + newBalance);
	
	            auditLogService.log(
	                    "BALANCE_UPDATED_FOR_USER",
	                    "User",
	                    user.getUserId(),
	                    actingUser.getUserId(),
	                    actingUser.getEmail(),
	                    getFirstRole(actingUser)
	            );
	        }
	
	        request.setStatus("APPROVED");
	        disbursalRequestRepo.save(request);
	
	        SalaryDisbursalApprovalHistory history = new SalaryDisbursalApprovalHistory();
	        history.setAction("APPROVED");
	        history.setComment(comment != null && !comment.isEmpty() ? 
	                          comment : "Approved by Bank Admin");
	        history.setActedBy(actingUser);
	        history.setDisbursal(request);
	        approvalHistoryRepo.save(history);
	
	        auditLogService.log(
	                "APPROVAL_HISTORY_SAVED",
	                "SalaryDisbursalApprovalHistory",
	                history.getHistoryId(),
	                actingUser.getUserId(),
	                actingUser.getEmail(),
	                getFirstRole(actingUser)
	        );
	
	        int slipsCreated = 0;
	        for (SalaryDisbursalLine line : request.getLines()) {
	            if ("PAID".equalsIgnoreCase(line.getStatus())) {
	                createSalarySlip(line, request.getPeriod());
	                slipsCreated++;
	
	                auditLogService.log(
	                        "SALARY_SLIP_CREATED",
	                        "SalarySlip",
	                        line.getLineId(),
	                        actingUser.getUserId(),
	                        actingUser.getEmail(),
	                        getFirstRole(actingUser)
	                );
	            }
	        }
	        
	        System.out.println("📄 Created " + slipsCreated + " salary slips");
	
	        sendApprovalNotification(request, comment);
	        
	        System.out.println("✅ Salary disbursal APPROVED for " + org.getOrgName() + 
	                         " | Processed: " + processedCount + " people | Amount: ₹" + totalAmount);
	    }
	
	    // ================= REJECT REQUEST =================
	    private void rejectRequest(SalaryDisbursalRequest request, User actingUser, String comment) {
	        System.out.println("❌ Rejecting salary disbursal for " + request.getOrganization().getOrgName() + 
	                         " | Period: " + request.getPeriod() + 
	                         " | Reason: " + (comment != null ? comment : "No reason provided"));
	        
	        request.setStatus("REJECTED");
	        disbursalRequestRepo.save(request);
	
	        SalaryDisbursalApprovalHistory history = new SalaryDisbursalApprovalHistory();
	        history.setAction("REJECTED");
	        history.setComment(comment != null && !comment.isEmpty() ? 
	                          comment : "Rejected by Bank Admin");
	        history.setActedBy(actingUser);
	        history.setDisbursal(request);
	        approvalHistoryRepo.save(history);
	
	        auditLogService.log(
	                "REJECTED_SALARY_DISBURSAL",
	                "SalaryDisbursalRequest",
	                request.getDisbursalId(),
	                actingUser.getUserId(),
	                actingUser.getEmail(),
	                getFirstRole(actingUser)
	        );
	
	        auditLogService.log(
	                "REJECTION_HISTORY_SAVED",
	                "SalaryDisbursalApprovalHistory",
	                history.getHistoryId(),
	                actingUser.getUserId(),
	                actingUser.getEmail(),
	                getFirstRole(actingUser)
	        );
	
	        sendRejectionNotification(request, comment);
	        
	        System.out.println("✅ Rejection notification sent to organization admins");
	    }
	
	    // ================= CREATE SALARY SLIP =================
	    private void createSalarySlip(SalaryDisbursalLine line, String period) {
	        SalarySlip.SalarySlipBuilder slipBuilder = SalarySlip.builder()
	                .period(period)
	                .disbursal(line.getDisbursalRequest())
	                .netAmount(line.getNetAmount());
	
	        if (line.getEmployee() != null) {
	            slipBuilder.employee(line.getEmployee());
	        } else if (line.getOrgAdmin() != null) {
	            slipBuilder.orgAdmin(line.getOrgAdmin());
	        }
	
	        SalarySlip slip = slipBuilder.build();
	        salarySlipRepository.save(slip);
	    }
	
	    // ================= SEND APPROVAL NOTIFICATION =================
	 // ✅ COMPLETE sendApprovalNotification method
	    private void sendApprovalNotification(SalaryDisbursalRequest request, String comment) {
	        Organization org = request.getOrganization();
	        
	        System.out.println("\n\n");
	        System.out.println("🔔════════════════════════════════════════");
	        System.out.println("🔔 SENDING APPROVAL NOTIFICATION");
	        System.out.println("🔔 Organization: " + org.getOrgName() + " (ID: " + org.getOrgId() + ")");
	        System.out.println("🔔════════════════════════════════════════");
	        
	        // ✅ Get users list from organization
	        List<User> orgUsers = org.getUsers();
	        
	        System.out.println("📋 Found " + orgUsers.size() + " users for organization");
	        
	        if (orgUsers == null || orgUsers.isEmpty()) {
	            System.err.println("❌ ERROR: Organization '" + org.getOrgName() + "' has NO users!");
	            return;
	        }
	        
	        // ✅ Find the organization user (with ROLE_ORGANIZATION)
	        User orgUser = orgUsers.stream()
	                .filter(u -> u.getRoles() != null && u.getRoles().stream()
	                        .anyMatch(r -> "ROLE_ORGANIZATION".equals(r.getRoleName())))
	                .findFirst()
	                .orElse(orgUsers.get(0)); // Fallback to first user
	        
	        System.out.println("✅ Organization User Found:");
	        System.out.println("   - User ID: " + orgUser.getUserId());
	        System.out.println("   - Email: " + orgUser.getEmail());
	        System.out.println("   - Roles: " + orgUser.getRoles().stream()
	                .map(Role::getRoleName)
	                .collect(Collectors.joining(", ")));
	        
	        String title = "✅ Salary Request Approved";
	        String message = String.format(
	            "Great news! Your salary disbursal request for period %s has been approved by Bank Admin.\n\n" +
	            "📊 Details:\n" +
	            "• Total People: %d\n" +
	            "• Total Amount: ₹%s\n" +
	            "• Status: Salary paid to all employees\n" +
	            "• Salary slips are now available for download\n\n" +
	            "%s",
	            request.getPeriod(),
	            request.getLines().size(),
	            request.getTotalAmount(),
	            comment != null && !comment.isEmpty() ? 
	                "💬 Bank Admin Comment: " + comment : ""
	        );
	        
	        try {
	            System.out.println("🚀 Creating notification for organization user...");
	            
	            notificationService.createInAppNotification(
	                orgUser,
	                title,
	                message,
	                request.getDisbursalId(),
	                "SALARY_DISBURSAL",
	                "HIGH"
	            );
	            
	            System.out.println("✅ Notification created successfully!");
	            
	        } catch (Exception e) {
	            System.err.println("❌ FAILED to create notification: " + e.getMessage());
	            e.printStackTrace();
	        }
	        
	        System.out.println("🔔════════════════════════════════════════\n\n");
	    }

	    // ✅ COMPLETE sendRejectionNotification method
	    private void sendRejectionNotification(SalaryDisbursalRequest request, String comment) {
	        Organization org = request.getOrganization();
	        
	        System.out.println("\n\n");
	        System.out.println("🔔════════════════════════════════════════");
	        System.out.println("🔔 SENDING REJECTION NOTIFICATION");
	        System.out.println("🔔 Organization: " + org.getOrgName());
	        System.out.println("🔔════════════════════════════════════════");
	        
	        List<User> orgUsers = org.getUsers();
	        
	        if (orgUsers == null || orgUsers.isEmpty()) {
	            System.err.println("❌ ERROR: Organization has NO users!");
	            return;
	        }
	        
	        User orgUser = orgUsers.stream()
	                .filter(u -> u.getRoles() != null && u.getRoles().stream()
	                        .anyMatch(r -> "ROLE_ORGANIZATION".equals(r.getRoleName())))
	                .findFirst()
	                .orElse(orgUsers.get(0));
	        
	        System.out.println("✅ Organization User: " + orgUser.getEmail());
	        
	        String title = "❌ Salary Request Rejected";
	        String message = String.format(
	            "Your salary disbursal request for period %s (₹%s) has been rejected by Bank Admin.\n\n" +
	            "📋 Rejection Reason:\n%s\n\n" +
	            "⚠️ Action Required:\n" +
	            "• Please review the rejection reason\n" +
	            "• Make necessary corrections\n" +
	            "• Resubmit the request if needed\n\n" +
	            "If you need assistance, please contact Bank Admin support.",
	            request.getPeriod(),
	            request.getTotalAmount(),
	            comment != null && !comment.isEmpty() ? comment : "No specific reason provided"
	        );
	        
	        try {
	            notificationService.createInAppNotification(
	                orgUser,
	                title,
	                message,
	                request.getDisbursalId(),
	                "SALARY_DISBURSAL",
	                "HIGH"
	            );
	            
	            System.out.println("✅ Rejection notification sent!");
	            
	        } catch (Exception e) {
	            System.err.println("❌ Failed to send rejection notification: " + e.getMessage());
	            e.printStackTrace();
	        }
	        
	        System.out.println("🔔════════════════════════════════════════\n\n");
	    }


	
	   
	
	    // ================= MAP TO DTO =================
	    private SalaryDisbursalRequestDTO mapToDTO(SalaryDisbursalRequest request) {
	        SalaryDisbursalRequestDTO dto = new SalaryDisbursalRequestDTO();
	        dto.setDisbursalId(request.getDisbursalId());
	        dto.setOrgId(request.getOrganization().getOrgId());
	        dto.setOrgName(request.getOrganization().getOrgName());
	        dto.setPeriod(request.getPeriod());
	        dto.setStatus(request.getStatus());
	        dto.setTotalAmount(request.getTotalAmount());
	        dto.setRemarks(request.getRemarks());
	        dto.setCreatedAt(request.getCreatedAt());
	
	        List<SalaryDisbursalLineDTO> lineDTOs = new ArrayList<>();
	        for (SalaryDisbursalLine line : request.getLines()) {
	            SalaryDisbursalLineDTO lineDTO = new SalaryDisbursalLineDTO();
	            lineDTO.setLineId(line.getLineId());
	
	            if (line.getEmployee() != null) {
	                lineDTO.setEmployeeName(line.getEmployee().getEmpName());
	                lineDTO.setEmployeeEmail(line.getEmployee().getEmpEmail());
	            } else if (line.getOrgAdmin() != null) {
	                lineDTO.setEmployeeName(line.getOrgAdmin().getName());
	                lineDTO.setEmployeeEmail(line.getOrgAdmin().getEmail());
	            }
	
	            lineDTO.setGrossSalary(line.getGrossSalary());
	            lineDTO.setDeductions(line.getDeductions());
	            lineDTO.setNetAmount(line.getNetAmount());
	            lineDTO.setStatus(line.getStatus());
	
	            lineDTOs.add(lineDTO);
	        }
	
	        dto.setLines(lineDTOs);
	        return dto;
	    }
	
	    // ================= UTILITY =================
	    private String getFirstRole(User user) {
	        return user.getRoles().stream()
	                .findFirst()
	                .map(Role::getRoleName)
	                .orElse("UNKNOWN");
	    }
	}
