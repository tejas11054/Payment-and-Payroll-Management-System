package com.paymentapp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.paymentapp.dto.SalaryGradeRequestDTO;
import com.paymentapp.dto.SalaryGradeResponseDTO;
import com.paymentapp.service.SalaryGradeService;

import lombok.RequiredArgsConstructor;
//changed
@RestController
@RequestMapping("/api/salary-grades")
@RequiredArgsConstructor
public class SalaryGradeController {

    private final SalaryGradeService salaryGradeService;

    @PostMapping("/{orgId}")
    public ResponseEntity<SalaryGradeResponseDTO> createSalaryGrade(
            @PathVariable Long orgId,
            @Validated @RequestBody SalaryGradeRequestDTO requestDTO) {
        SalaryGradeResponseDTO responseDTO = salaryGradeService.createSalaryGrade(orgId, requestDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{orgId}/{gradeId}")
    public ResponseEntity<SalaryGradeResponseDTO> updateSalaryGrade(
            @PathVariable Long orgId,
            @PathVariable Long gradeId,
            @Validated @RequestBody SalaryGradeRequestDTO requestDTO) {
        SalaryGradeResponseDTO responseDTO = salaryGradeService.updateSalaryGrade(orgId, gradeId, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{orgId}/{gradeId}")
    public ResponseEntity<SalaryGradeResponseDTO> getSalaryGrade(
            @PathVariable Long orgId,
            @PathVariable Long gradeId) {
        SalaryGradeResponseDTO responseDTO = salaryGradeService.getSalaryGradeById(orgId, gradeId);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{orgId}")
    public ResponseEntity<List<SalaryGradeResponseDTO>> getAllSalaryGrades(
            @PathVariable Long orgId) {
        List<SalaryGradeResponseDTO> grades = salaryGradeService.getAllSalaryGrades(orgId);
        return ResponseEntity.ok(grades);
    }

    @DeleteMapping("/{orgId}/{gradeId}")
    public ResponseEntity<Void> deleteSalaryGrade(
            @PathVariable Long orgId,
            @PathVariable Long gradeId) {
        salaryGradeService.deleteSalaryGrade(orgId, gradeId);
        return ResponseEntity.noContent().build();
    }
}
