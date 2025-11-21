package com.paymentapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paymentapp.dto.OrganizationResponseDTO;
import com.paymentapp.dto.RejectRequest;
import com.paymentapp.dto.SalaryDisbursalRequestDTO;
import com.paymentapp.entity.ContactMessage;
import com.paymentapp.entity.User;
import com.paymentapp.security.CustomUserDetails;
import com.paymentapp.service.BankAdminService;
import com.paymentapp.service.SalaryDisbursalApprovalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bank-admin")
@RequiredArgsConstructor
public class BankAdminController {

    private final BankAdminService bankAdminService;
    private final SalaryDisbursalApprovalService salaryDisbursalApprovalService;

    @GetMapping("/organizations")
    public ResponseEntity<List<OrganizationResponseDTO>> getAllApprovedOrganizations() {
        List<OrganizationResponseDTO> organizations = bankAdminService.getAllOrganizations();
        return ResponseEntity.ok(organizations);
    }

    @GetMapping("/pending-organizations")
    public ResponseEntity<List<OrganizationResponseDTO>> getPendingOrgs() {
        return ResponseEntity.ok(bankAdminService.getPendingOrganizations());
    }

    @PutMapping("/{orgId}/approve")
    public ResponseEntity<String> approveOrg(
            @PathVariable Long orgId,
            @AuthenticationPrincipal CustomUserDetails performingUserDetails) {  

        User performingUser = performingUserDetails.getUser();
        bankAdminService.approveOrganization(orgId, performingUser);
        return ResponseEntity.ok("Organization approved.");
    }

    @PutMapping("/{orgId}/reject")
    public ResponseEntity<?> rejectOrganization(
            @PathVariable Long orgId,
            @Valid @RequestBody RejectRequest request,
            @AuthenticationPrincipal CustomUserDetails performingUserDetails) {

        User performingUser = performingUserDetails.getUser();
        bankAdminService.rejectOrganization(orgId, request.getReason(), performingUser);
        return ResponseEntity.ok("Organization rejected successfully");
    }


    @GetMapping("/delete-requests")
    public ResponseEntity<List<OrganizationResponseDTO>> getDeleteRequestedOrgs() {
        return ResponseEntity.ok(bankAdminService.getOrganizationsRequestedForDeletion());
    }

    @PutMapping("/{orgId}/handle-deletion")
    public ResponseEntity<?> handleDeletionRequest(
            @PathVariable Long orgId,
            @RequestParam boolean approve,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal CustomUserDetails performingUserDetails) {  

        User performingUser = performingUserDetails.getUser();
        bankAdminService.handleDeletionRequest(orgId, approve, reason, performingUser);
        return ResponseEntity.ok("Deletion request processed successfully");
    }
    
    @GetMapping("/contact-messages")
    public ResponseEntity<List<ContactMessage>> getContactMessages() {
        return ResponseEntity.ok(bankAdminService.getContactMessages());
    }

    @GetMapping("/salary-requests/pending")
    public ResponseEntity<List<SalaryDisbursalRequestDTO>> getPendingSalaryRequests() {
        List<SalaryDisbursalRequestDTO> requests = salaryDisbursalApprovalService.getPendingRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/salary-requests/{disbursalId}")
    public ResponseEntity<SalaryDisbursalRequestDTO> getSalaryRequestDetails(@PathVariable Long disbursalId) {
        SalaryDisbursalRequestDTO request = salaryDisbursalApprovalService.getRequestDetails(disbursalId);
        return ResponseEntity.ok(request);
    }

}
