package com.paymentapp.controller;

import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.paymentapp.dto.EmployeeRequestDTO;
import com.paymentapp.dto.EmployeeResponseDTO;
import com.paymentapp.entity.Employee;
import com.paymentapp.entity.SalaryGrade;
import com.paymentapp.entity.User;
import com.paymentapp.repository.EmployeeRepository;
import com.paymentapp.repository.UserRepository;
import com.paymentapp.security.CustomUserDetails;
import com.paymentapp.service.EmployeeConcernService;
import com.paymentapp.service.EmployeeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

	private static final String ROLE_ORG_ADMIN = "ROLE_ORG_ADMIN";
	private static final String ROLE_ORGANIZATION = "ROLE_ORGANIZATION";

	private final EmployeeService employeeService;
	private final UserRepository userRepository;
	private final EmployeeConcernService concernService;
	private final EmployeeRepository employeeRepo;

	@PostMapping(value = "/{orgId}/employees", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<EmployeeResponseDTO> createEmployee(@PathVariable Long orgId,
			@Valid @RequestPart("employee") EmployeeRequestDTO employeeRequest,
			@RequestPart(value = "document", required = false) MultipartFile documentFile,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		System.out.println("✅ createEmployee() called for orgId = " + orgId);

		User performingUser = userDetails.getUser();
		if (performingUser == null) {
			throw new RuntimeException("Authenticated user not found. Please log in again.");
		}

		EmployeeResponseDTO response = employeeService.createEmployee(orgId, employeeRequest, documentFile,
				performingUser);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{orgId}/employees/bulk-upload")
	public ResponseEntity<?> uploadEmployeesBulk(@PathVariable Long orgId,
			@RequestParam("departmentName") String departmentName,
			@RequestParam(value = "salaryGradeId", required = false) Long salaryGradeId,
			@RequestParam("file") MultipartFile file, @RequestParam(value = "fileUrl", required = false) String fileUrl,
			@RequestParam(value = "documentFile", required = false) MultipartFile documentFile,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		try {
			if (file == null || file.isEmpty()) {
				return ResponseEntity.badRequest().body("File must be provided");
			}

			InputStream inputStream = file.getInputStream();
			String fileName = file.getOriginalFilename();

			List<EmployeeResponseDTO> createdEmployees = employeeService.createEmployeesBulk(orgId, departmentName,
					salaryGradeId, inputStream, fileName, fileUrl, documentFile, userDetails.getUser());

			return ResponseEntity.ok(createdEmployees);

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body("Invalid request: " + e.getMessage());

		} catch (RuntimeException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server Error: " + e.getMessage());

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
		}
	}

	@GetMapping("/org/{orgId}")
	public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees(@PathVariable Long orgId) {
		List<EmployeeResponseDTO> employees = employeeService.getAllEmployees(orgId);
		return ResponseEntity.ok(employees);
	}

	@GetMapping("/{orgId}/{empId}")
	public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long orgId, @PathVariable Long empId,
			@RequestParam(required = false) Long departmentId, @AuthenticationPrincipal CustomUserDetails userDetails) {

		User performingUser = userDetails.getUser();
		EmployeeResponseDTO employee = employeeService.getEmployeeById(empId, departmentId, performingUser);
		return ResponseEntity.ok(employee);
	}

	@PutMapping("/{orgId}/employees/{empId}")
	public ResponseEntity<EmployeeResponseDTO> updateEmployee(@PathVariable Long orgId, @PathVariable Long empId,
			@Valid @RequestBody EmployeeRequestDTO employeeRequest,
			@AuthenticationPrincipal CustomUserDetails userDetails) throws AccessDeniedException {

		User performingUser = userDetails.getUser();

		boolean hasUpdatePermission = performingUser.getRoles().stream().anyMatch(
				role -> role.getRoleName().equals(ROLE_ORGANIZATION) || role.getRoleName().equals(ROLE_ORG_ADMIN));

		if (!hasUpdatePermission) {
			throw new AccessDeniedException("You do not have permission to update employee details");
		}

		EmployeeResponseDTO updated = employeeService.updateEmployee(empId, employeeRequest, performingUser);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/{orgId}/employees/{empId}")
	public ResponseEntity<Void> deleteEmployee(@PathVariable Long orgId, @PathVariable Long empId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		User performingUser = userDetails.getUser();
		if (performingUser == null) {
			throw new RuntimeException("Authenticated user not found");
		}

		employeeService.deleteEmployee(empId, performingUser);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{empId}/dashboard")
	public ResponseEntity<Map<String, Object>> getEmployeeDashboard(
	        @PathVariable Long empId,
	        @AuthenticationPrincipal CustomUserDetails userDetails) {
	    
	    System.out.println("📊 Getting dashboard for empId: " + empId);
	    
	    Employee employee = employeeRepo.findById(empId)
	            .orElseThrow(() -> new RuntimeException("Employee not found"));
	    
	    Map<String, Object> response = new HashMap<>();
	    response.put("empId", employee.getEmpId());
	    response.put("empName", employee.getEmpName());
	    response.put("empEmail", employee.getEmpEmail());
	    response.put("phone", employee.getPhone());
	    response.put("status", employee.getStatus());
	    response.put("bankAccountNo", employee.getBankAccountNo());
	    response.put("ifscCode", employee.getIfscCode());
	    
	    if (employee.getOrganization() != null) {
	        response.put("orgName", employee.getOrganization().getOrgName());
	    }
	    
	    if (employee.getDepartment() != null) {
	        response.put("departmentName", employee.getDepartment().getName());
	    }
	    
	    if (employee.getSalaryGrade() != null) {
	        SalaryGrade grade = employee.getSalaryGrade();
	        response.put("gradeCode", grade.getGradeCode());
	        response.put("basicSalary", grade.getBasicSalary());
	        response.put("hra", grade.getHra());
	        response.put("da", grade.getDa());
	        response.put("allowances", grade.getAllowances());
	        response.put("pf", grade.getPf());
	    }
	    
	    System.out.println("✅ Dashboard data sent");
	    
	    return ResponseEntity.ok(response);
	}

}
