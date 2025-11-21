package com.paymentapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paymentapp.dto.SalaryDisbursalApprovalRequestDTO;
import com.paymentapp.service.SalaryDisbursalApprovalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/salary-disbursal")
@RequiredArgsConstructor
public class SalaryDisbursalApprovalController {

    private final SalaryDisbursalApprovalService approvalService;

   
    @PostMapping("/approve-or-reject")
    public ResponseEntity<?> approveOrRejectDisbursal(@RequestBody SalaryDisbursalApprovalRequestDTO requestDTO) {
        try {
            approvalService.processApproval(requestDTO);
            return ResponseEntity.ok("Salary disbursal request processed successfully.");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body("Error: " + ex.getMessage());
        }
    }

}
