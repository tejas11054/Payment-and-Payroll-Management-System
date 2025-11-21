package com.paymentapp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paymentapp.dto.DepartmentRequestDTO;
import com.paymentapp.dto.DepartmentResponseDTO;
import com.paymentapp.dto.EmployeeResponseDTO;
import com.paymentapp.entity.User;
import com.paymentapp.security.CustomUserDetails;
import com.paymentapp.service.DepartmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
//changed
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping("/{orgId}")
    public ResponseEntity<DepartmentResponseDTO> createDepartment(
            @PathVariable Long orgId,
            @Valid @RequestBody DepartmentRequestDTO dto) {
        
        DepartmentResponseDTO response = departmentService.createDepartment(orgId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{departmentId}")
    public ResponseEntity<DepartmentResponseDTO> getById(@PathVariable Long departmentId) {
        return ResponseEntity.ok(departmentService.getDepartmentById(departmentId));
    }

    @GetMapping("/org/{orgId}")
    public ResponseEntity<List<DepartmentResponseDTO>> getAllByOrg(@PathVariable Long orgId) {
        return ResponseEntity.ok(departmentService.getAllDepartments(orgId));
    }

    @GetMapping("/org/{orgId}/name")
    public ResponseEntity<DepartmentResponseDTO> getByName(
            @PathVariable Long orgId,
            @RequestParam("name") String name) {
        
        return ResponseEntity.ok(departmentService.getDepartmentByName(orgId, name));
    }

    @PutMapping("/{departmentId}")
    public ResponseEntity<DepartmentResponseDTO> update(
            @PathVariable Long departmentId,
            @Valid @RequestBody DepartmentRequestDTO dto) {
        
        return ResponseEntity.ok(departmentService.updateDepartment(departmentId, dto));
    }

    @DeleteMapping("/{departmentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long departmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {  // ✅ ADDED
        
        User performingUser = userDetails.getUser();
        if (performingUser == null) {
            throw new RuntimeException("Authenticated user not found. Please login again.");
        }
        
        departmentService.deleteDepartment(departmentId, performingUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{departmentId}/employees")
    public ResponseEntity<List<EmployeeResponseDTO>> getEmployeesByDepartment(
            @PathVariable Long departmentId) {
        
        List<EmployeeResponseDTO> employees = departmentService.getEmployeesByDepartment(departmentId);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{departmentId}/employee-count")
    public ResponseEntity<Long> getActiveEmployeeCount(@PathVariable Long departmentId) {
        long count = departmentService.getActiveEmployeeCountByDepartment(departmentId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{departmentId}/admin-count")
    public ResponseEntity<Long> getActiveAdminCount(@PathVariable Long departmentId) {
        long count = departmentService.getActiveAdminCountByDepartment(departmentId);
        return ResponseEntity.ok(count);
    }
}
