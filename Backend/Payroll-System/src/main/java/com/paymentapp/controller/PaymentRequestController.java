package com.paymentapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paymentapp.dto.PaymentApprovalDTO;
import com.paymentapp.dto.PaymentRequestDTO;
import com.paymentapp.entity.User;
import com.paymentapp.repository.UserRepository;
import com.paymentapp.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment-requests")
@RequiredArgsConstructor
public class PaymentRequestController {

    private final PaymentService paymentService;
    private final UserRepository userRepo;

    @PostMapping
    public ResponseEntity<PaymentRequestDTO> createRequest(@RequestBody PaymentRequestDTO dto) {
        PaymentRequestDTO created = paymentService.createRequest(dto);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('BANK_ADMIN')")
    public ResponseEntity<List<PaymentRequestDTO>> getPendingRequests() {
        List<PaymentRequestDTO> pendingRequests = paymentService.getPendingRequests();
        return ResponseEntity.ok(pendingRequests);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentRequestDTO> getRequestById(@PathVariable Long paymentId) {
        PaymentRequestDTO request = paymentService.getRequestById(paymentId);
        return ResponseEntity.ok(request);
    }

    @GetMapping("/org/{orgId}")
    public ResponseEntity<List<PaymentRequestDTO>> getRequestsByOrg(@PathVariable Long orgId) {
        List<PaymentRequestDTO> requests = paymentService.getRequestsByOrg(orgId);
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/{paymentId}/approve")
    @PreAuthorize("hasRole('BANK_ADMIN')")
    public ResponseEntity<PaymentRequestDTO> approveRequest(
            @PathVariable Long paymentId,
            @RequestBody(required = false) PaymentApprovalDTO dto) {
        
        User dummyApprover = userRepo.findById(1L).orElseThrow();
        
        PaymentRequestDTO updated = paymentService.approveRequest(paymentId, dto, dummyApprover);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{paymentId}/reject")
    @PreAuthorize("hasRole('BANK_ADMIN')")
    public ResponseEntity<PaymentRequestDTO> rejectRequest(
            @PathVariable Long paymentId,
            @RequestBody PaymentApprovalDTO dto) {
        
        User dummyApprover = userRepo.findById(1L).orElseThrow();
        
        PaymentRequestDTO updated = paymentService.rejectRequest(paymentId, dto, dummyApprover);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    @PreAuthorize("hasRole('BANK_ADMIN')")
    public ResponseEntity<List<PaymentRequestDTO>> getAllRequests() {
        List<PaymentRequestDTO> allRequests = paymentService.getAllRequests();
        return ResponseEntity.ok(allRequests);
    }
}
