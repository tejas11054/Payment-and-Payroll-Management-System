// PaymentController.java
package com.paymentapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.paymentapp.security.CustomUserDetails;
import com.paymentapp.service.OrganizationService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")

public class PaymentController {
    
    @Autowired
    private OrganizationService organizationService;
    

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Payment service is running");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/mock-payment")
    public ResponseEntity<?> mockPayment(
        @RequestParam Long orgId,
        @RequestParam BigDecimal amount,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            if (amount.compareTo(BigDecimal.valueOf(10000)) < 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Minimum amount is ₹10,000"));
            }
            
            if (amount.compareTo(BigDecimal.valueOf(10000000)) > 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Maximum amount is ₹1,00,00,000"));
            }
            
  
            organizationService.addBalance(orgId, amount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Balance added successfully");
            response.put("amount", amount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
   
    @GetMapping("/balance/{orgId}")
    public ResponseEntity<?> getBalance(@PathVariable Long orgId) {
        try {
            BigDecimal balance = organizationService.getAccountBalancebyOrgId(orgId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("balance", balance);
            response.put("orgId", orgId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
}
