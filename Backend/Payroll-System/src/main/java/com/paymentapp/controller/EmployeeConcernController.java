package com.paymentapp.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.paymentapp.dto.EmployeeConcernRequestDTO;
import com.paymentapp.dto.EmployeeConcernResponseDTO;
import com.paymentapp.dto.EmployeeConcernSolveRequestDTO;
import com.paymentapp.entity.EmployeeConcern;
import com.paymentapp.repository.EmployeeConcernRepository;
import com.paymentapp.service.EmployeeConcernService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/concerns")
@RequiredArgsConstructor
public class EmployeeConcernController {

    private final EmployeeConcernService concernService;
    private final EmployeeConcernRepository concernRepository;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<EmployeeConcernResponseDTO> raiseConcern(
            @RequestPart("data") EmployeeConcernRequestDTO requestDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        EmployeeConcernResponseDTO responseDTO = concernService.raiseConcern(requestDTO, file);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }
    
    @PutMapping("/{concernId}/resolve")
    public ResponseEntity<EmployeeConcernResponseDTO> resolveConcern(
            @PathVariable Long concernId,
            @RequestBody EmployeeConcernSolveRequestDTO solveRequest) {

        EmployeeConcernResponseDTO response = concernService.solveConcern(
            concernId,
            solveRequest.getResolvedByOrgAdminId(),
            solveRequest.getResponseText()
        );
        return ResponseEntity.ok(response);
    }
    @GetMapping("/employee/{employeeId}/pending/count")
    public ResponseEntity<Long> getPendingConcernsCount(@PathVariable Long employeeId) {
        long count = concernService.countPendingConcernsByEmployee(employeeId);
        return ResponseEntity.ok(count);
    }


    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<EmployeeConcernResponseDTO>> getEmployeeConcerns(
            @PathVariable Long employeeId) {
        
        System.out.println("📋 Getting concerns for employeeId: " + employeeId);
        
        List<EmployeeConcern> concerns = concernRepository.findByEmployee_EmpIdOrderByRaisedAtDesc(employeeId);
        
        List<EmployeeConcernResponseDTO> dtos = concerns.stream()
                .map(concern -> {
                    EmployeeConcernResponseDTO dto = new EmployeeConcernResponseDTO();
                    dto.setConcernId(concern.getConcernId());
                    dto.setDescription(concern.getDescription());
                    dto.setAttachmentUrl(concern.getAttachmentUrl());
                    dto.setStatus(concern.getStatus());
                    dto.setRaisedAt(concern.getRaisedAt());
                    dto.setEmpid(concern.getEmployee().getEmpId());
                    return dto;
                })
                .collect(Collectors.toList());
        
        System.out.println("✅ Found " + dtos.size() + " concerns");
        
        return ResponseEntity.ok(dtos);
    }

}
