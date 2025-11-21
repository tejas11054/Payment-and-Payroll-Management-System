package com.paymentapp.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paymentapp.dto.SalaryDisbursalRequestCreateDTO;
import com.paymentapp.dto.SalaryDisbursalRequestDTO;
import com.paymentapp.service.SalaryDisbursalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/salary-disbursal")
@RequiredArgsConstructor
public class SalaryDisbursalController {

    private final SalaryDisbursalService salaryDisbursalService;

    @PostMapping("/request")
    public ResponseEntity<?> createDisbursal(@RequestBody SalaryDisbursalRequestCreateDTO dto) {
        try {
            SalaryDisbursalRequestDTO response = salaryDisbursalService.createCustomSalaryDisbursal(dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}
