package com.paymentapp.serviceImpl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.paymentapp.dto.EmployeeRequestDTO;
import com.paymentapp.dto.EmployeeResponseDTO;
import com.paymentapp.dto.SalaryGradeResponseDTO;
import com.paymentapp.entity.Department;
import com.paymentapp.entity.Employee;
import com.paymentapp.entity.Organization;
import com.paymentapp.entity.Role;
import com.paymentapp.entity.SalaryGrade;
import com.paymentapp.entity.UploadBatch;
import com.paymentapp.entity.UploadBatchLine;
import com.paymentapp.entity.User;
import com.paymentapp.repository.DepartmentRepository;
import com.paymentapp.repository.EmployeeRepository;
import com.paymentapp.repository.OrganizationRepository;
import com.paymentapp.repository.RoleRepository;
import com.paymentapp.repository.SalaryGradeRepository;
import com.paymentapp.repository.UploadBatchLineRepository;
import com.paymentapp.repository.UploadBatchRepository;
import com.paymentapp.repository.UserRepository;
import com.paymentapp.service.AuditLogService;
import com.paymentapp.service.DocumentService;
import com.paymentapp.service.EmployeeService;
import com.paymentapp.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

	private final EmployeeRepository employeeRepository;
	private final OrganizationRepository organizationRepository;
	private final DepartmentRepository departmentRepository;
	private final UploadBatchRepository uploadBatchRepository;
	private final SalaryGradeRepository salaryGradeRepository;
	private final UploadBatchLineRepository uploadBatchLineRepository;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final DocumentService documentService;
	private final NotificationService notificationService;
	private final AuditLogService auditLogService;
	private final ModelMapper modelMapper;
	private final PasswordEncoder passwordEncoder;

	private static final SecureRandom secureRandom = new SecureRandom();
	private static final String ROLE_EMPLOYEE = "ROLE_EMPLOYEE";
	private static final String ROLE_ORG_ADMIN = "ROLE_ORG_ADMIN";
	private static final int MAX_EMAIL_LENGTH = 254;
	private static final String ROLE_ORGANIZATION = "ROLE_ORGANIZATION";

	private Department getDepartmentForOrgAdmin(User user) {

		return employeeRepository.findByUser(user).map(Employee::getDepartment).orElse(null); 
																								
	}

	private boolean isOrgAdmin(User user) {
		return user.getRoles().stream().anyMatch(r -> r.getRoleName().equals(ROLE_ORG_ADMIN));
	}

	@Override
	@Transactional
	public EmployeeResponseDTO createEmployee(Long orgId, EmployeeRequestDTO dto, MultipartFile documentFile,
	        User performingUser) {
	    
	    Organization organization = organizationRepository.findById(orgId)
	        .orElseThrow(() -> new RuntimeException("Organization not found"));

	 
	    if (dto.getDepartmentName() == null || dto.getDepartmentName().isBlank()) {
	        throw new RuntimeException("Department name must be provided in the request");
	    }

	    Department department = departmentRepository
	        .findByNameIgnoreCaseAndOrganizationAndDeletedFalse(dto.getDepartmentName().trim(), organization)
	        .orElseThrow(() -> new RuntimeException(
	            "Active department not found with name: " + dto.getDepartmentName()));

	    // Email validation
	    String email = Optional.ofNullable(dto.getEmpEmail())
	        .map(String::trim)
	        .map(String::toLowerCase)
	        .orElseThrow(() -> new RuntimeException("Email must not be null"));

	    if (userRepository.existsByEmail(email)) {
	        throw new RuntimeException("Email already exists");
	    }


	    SalaryGrade salaryGrade = null;
	    if (dto.getSalaryGradeId() != null && dto.getSalaryGradeId() > 0) {
	        salaryGrade = salaryGradeRepository.findById(dto.getSalaryGradeId())
	            .orElseThrow(() -> new RuntimeException("SalaryGrade not found with id: " + dto.getSalaryGradeId()));
	        
	        // Validate salary grade belongs to organization
	        if (!salaryGrade.getOrganization().getOrgId().equals(orgId)) {
	            throw new RuntimeException("Salary grade does not belong to this organization");
	        }
	    }

	    // Create user account
	    String pwd = generatePassword(organization.getOrgName(), dto.getEmpName());
	    Role role = roleRepository.findByRoleName(ROLE_EMPLOYEE)
	        .orElseThrow(() -> new RuntimeException("ROLE_EMPLOYEE not found in roles table."));

	    User user = new User();
	    user.setEmail(email);
	    user.setPassword(passwordEncoder.encode(pwd));
	    user.setOrganization(organization);
	    user.setRoles(Set.of(role));
	    user.setStatus("ACTIVE");
	    user = userRepository.save(user);

	    // Create employee
	    Employee emp = new Employee();
	    emp.setEmpName(dto.getEmpName());
	    emp.setEmpEmail(email);
	    emp.setPhone(dto.getPhone());
	    emp.setBankAccountName(dto.getBankAccountName());
	    emp.setBankAccountNo(dto.getBankAccountNo());
	    emp.setIfscCode(dto.getIfscCode());
	    emp.setStatus("ACTIVE");
	    emp.setOrganization(organization);
	    emp.setUser(user);
	    emp.setDeleted(false);
	    emp.setDepartment(department);
	    emp.setSalaryGrade(salaryGrade);
	    emp = employeeRepository.save(emp);
	    
	    user.setEmployee(emp);

	    // Upload document
	    if (documentFile != null && !documentFile.isEmpty()) {
	        try {
	            documentService.uploadAndSaveDocument(documentFile, "EMPLOYEE", emp.getEmpId(), 
	                "PROFILE_DOCUMENT", performingUser.getUserId(), organization);
	        } catch (Exception e) {
	            System.err.println("Document upload failed for empId " + emp.getEmpId() + ": " + e.getMessage());
	        }
	    }

	    // Send welcome email
	    try {
	        notificationService.sendEmail(email, "Welcome to PaymentApp - Employee Account Created", 
	            String.format("Dear %s,\n\nYour employee account has been created successfully.\n" +
	                "Your default password is: %s\nPlease change your password after login.\n\n" +
	                "Best,\nPaymentApp Team", dto.getEmpName(), pwd));
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    // Audit log
	    try {
	        auditLogService.log("CREATE_EMPLOYEE", "EMPLOYEE", user.getUserId(), 
	            performingUser.getUserId(), performingUser.getEmail(), ROLE_EMPLOYEE);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    // ✅ Map response with department and grade info
	    EmployeeResponseDTO response = modelMapper.map(emp, EmployeeResponseDTO.class);
	    response.setDepartmentName(department.getName());
	    if (salaryGrade != null) {
	        response.setSalaryGradeId(salaryGrade.getGradeId());
	        response.setGradeCode(salaryGrade.getGradeCode());
	    }

	    return response;
	}



	@Override
	@Transactional
	public List<EmployeeResponseDTO> createEmployeesBulk(Long orgId, String departmentName, Long salaryGradeId,
			InputStream fileInputStream, String fileName, String fileUrl, MultipartFile documentFile,
			User performingUser) throws IOException {

		if (fileName == null || fileName.isBlank()) {
			throw new RuntimeException("File name must be provided");
		}

		BufferedInputStream bis = new BufferedInputStream(fileInputStream);
		List<EmployeeRequestDTO> dtos;
		UserDataImportService importService = new UserDataImportService();

		if (fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls")) {
			dtos = importService.parseEmployeeExcel(bis);
		} else if (fileName.toLowerCase().endsWith(".csv")) {
			dtos = importService.parseEmployeeCsv(bis);
		} else {
			throw new RuntimeException("Unsupported file type: " + fileName);
		}

		if (dtos.isEmpty()) {
			throw new RuntimeException("No valid records found in uploaded file");
		}

		Organization organization = organizationRepository.findById(orgId)
				.orElseThrow(() -> new RuntimeException("Organization not found"));

		if (organization.getStatus() == null || organization.getStatus().trim().equalsIgnoreCase("REJECTED")) {
			throw new RuntimeException("Operation denied: Organization is rejected");
		}

		Department department = departmentRepository
				.findByNameIgnoreCaseAndOrganizationAndDeletedFalse(departmentName.trim(), organization).orElseThrow(
						() -> new RuntimeException("Active department not found with name: " + departmentName.trim()));
		SalaryGrade salaryGrade = null;
		if (salaryGradeId != null && salaryGradeId > 0) {
			salaryGrade = salaryGradeRepository.findById(salaryGradeId)
					.orElseThrow(() -> new RuntimeException("Salary grade not found with id: " + salaryGradeId));
		}
		UploadBatch batch = new UploadBatch();
		batch.setUploadedBy(performingUser.getUserId());
		batch.setFileUrl(fileUrl != null && !fileUrl.isBlank() ? fileUrl : "Uploaded Directly");
		batch.setOrganization(organization);
		batch.setProcessedCount(0);
		batch.setStatus("PENDING");
		batch.setRecordCount(dtos.size());
		batch.setEntityType("EMPLOYEE");

		int rowNo = 1;
		for (EmployeeRequestDTO dto : dtos) {
			String rawData = dto.getEmpEmail() != null ? dto.getEmpEmail() : "";
			UploadBatchLine line = UploadBatchLine.builder().rowNumber(rowNo++).rawData(rawData).status("PENDING")
					.message(null).entityType("EMPLOYEE").uploadBatch(batch).build();
			batch.addBatchLine(line);
		}
		batch = uploadBatchRepository.save(batch);

		List<EmployeeResponseDTO> createdList = new ArrayList<>();
		int processedCount = 0;

		for (int i = 0; i < dtos.size(); i++) {
			EmployeeRequestDTO dto = dtos.get(i);
			UploadBatchLine line = batch.getLines().get(i);

			// ✅ Validation checks
			if (dto.getEmpEmail() == null || dto.getEmpEmail().isBlank()) {
				line.setStatus("FAILED");
				line.setMessage("Email is missing");
				uploadBatchLineRepository.save(line);
				continue;
			}

			if (dto.getEmpEmail().length() > MAX_EMAIL_LENGTH) {
				line.setStatus("FAILED");
				line.setMessage("Email too long");
				uploadBatchLineRepository.save(line);
				continue;
			}

			String normalizedEmail = dto.getEmpEmail().trim().toLowerCase();
			if (userRepository.existsByEmail(normalizedEmail)) {
				line.setStatus("FAILED");
				line.setMessage("Duplicate email");
				uploadBatchLineRepository.save(line);
				continue;
			}

			try {
				String email = dto.getEmpEmail().trim().toLowerCase();
				String pwd = generatePassword(organization.getOrgName(), dto.getEmpName());
				Role role = roleRepository.findByRoleName(ROLE_EMPLOYEE)
						.orElseThrow(() -> new RuntimeException("Role not found"));

				User user = new User();
				user.setEmail(email);
				user.setPassword(passwordEncoder.encode(pwd));
				user.setOrganization(organization);
				user.setRoles(Set.of(role));
				user.setStatus("ACTIVE");
				user = userRepository.save(user);

				Employee emp = new Employee();
				emp.setEmpName(dto.getEmpName());
				emp.setEmpEmail(email);
				emp.setPhone(dto.getPhone());
				emp.setBankAccountName(dto.getBankAccountName()); // ✅ NEW - Set from CSV/Excel
				emp.setBankAccountNo(dto.getBankAccountNo());
				emp.setIfscCode(dto.getIfscCode());
				emp.setStatus("ACTIVE");
				emp.setOrganization(organization);
				emp.setUser(user);
				emp.setDeleted(false);
				emp.setDepartment(department);
				emp.setSalaryGrade(salaryGrade); // ✅ No salary grade in bulk upload

				emp = employeeRepository.save(emp);
				user.setEmployee(emp);
				
				if (dto.getDocumentUrl() != null && !dto.getDocumentUrl().isBlank()) {
				    try {
				        documentService.uploadAndSaveDocumentFromUrl(
				            dto.getDocumentUrl(),
				            "EMPLOYEE",
				            emp.getEmpId(),
				            "BULK_UPLOAD_DOCUMENT",
				            performingUser.getUserId(),
				            organization
				        );
				    } catch (Exception ex) {
				        System.err.println("Failed to upload document for empId=" + emp.getEmpId() + ": " + ex.getMessage());
				    }
				}


				EmployeeResponseDTO resp = modelMapper.map(emp, EmployeeResponseDTO.class);

				line.setEntityId(resp.getEmpId());
				line.setStatus("SUCCESS");
				line.setMessage("Created successfully");
				uploadBatchLineRepository.save(line);

				 auditLogService.log(
			                "CREATE_EMPLOYEE",
			                "EMPLOYEE",
			                user.getUserId(),
			                performingUser.getUserId(),
			                performingUser.getEmail(),
			                performingUser.getRoles().stream().map(Role::getRoleName).findFirst().orElse("UNKNOWN")
			            );
				 
				createdList.add(resp);
				processedCount++;

			} catch (Exception e) {
				line.setStatus("FAILED");
				line.setMessage("Error: " + e.getMessage());
				uploadBatchLineRepository.save(line);
			}
		}

		batch.setProcessedCount(processedCount);
		batch.setStatus(processedCount == dtos.size() ? "COMPLETED" : "PARTIALLY_COMPLETED");
		uploadBatchRepository.save(batch);

		 auditLogService.log(
			        "BULK_CREATE_EMPLOYEE",
			        "EMPLOYEE",
			        performingUser.getUserId(),
			        performingUser.getUserId(),
			        performingUser.getEmail(),
			        performingUser.getRoles().stream().findFirst().map(Role::getRoleName).orElse("UNKNOWN")
			    );
		 
		return createdList;
	}

	@Override
	public List<EmployeeResponseDTO> getAllEmployees(Long orgId) {
	    System.out.println("📋 Getting all employees for orgId: " + orgId);
	    
	    Organization organization = organizationRepository.findById(orgId)
	            .orElseThrow(() -> new RuntimeException("Organization not found"));

	    List<Employee> employees = employeeRepository.findByOrganization(organization);
	    
	    System.out.println("✅ Found " + employees.size() + " employees");

	    return employees.stream()
	            .map(emp -> {
	                EmployeeResponseDTO dto = new EmployeeResponseDTO();
	                
	                // Basic fields
	                dto.setEmpId(emp.getEmpId());
	                dto.setEmpName(emp.getEmpName());
	                dto.setEmpEmail(emp.getEmpEmail());
	                dto.setPhone(emp.getPhone());
	                dto.setStatus(emp.getStatus());
	                dto.setBankAccountName(emp.getBankAccountName());
	                dto.setBankAccountNo(emp.getBankAccountNo());
	                dto.setIfscCode(emp.getIfscCode());
	                dto.setOrganizationId(orgId);
	                
	                // ✅ Handle department
	                try {
	                    if (emp.getDepartment() != null && !emp.getDepartment().isDeleted()) {
	                        dto.setDepartmentName(emp.getDepartment().getName());
	                        dto.setDepartmentId(emp.getDepartment().getDepartmentId());
	                    } else {
	                        dto.setDepartmentName("Deleted Department");
	                        dto.setDepartmentId(null);
	                    }
	                } catch (Exception e) {
	                    System.out.println("⚠️ Error accessing department for employee " + emp.getEmpId());
	                    dto.setDepartmentName(null);
	                    dto.setDepartmentId(null);
	                }
	                
	                // ✅✅✅ Handle salary grade - Reuse SalaryGradeResponseDTO
	                try {
	                    if (emp.getSalaryGrade() != null) {
	                        SalaryGrade grade = emp.getSalaryGrade();
	                        
	                        // Set basic references
	                        dto.setSalaryGradeId(grade.getGradeId());
	                        dto.setGradeCode(grade.getGradeCode());
	                        
	                        // ✅ CREATE SalaryGradeResponseDTO
	                        SalaryGradeResponseDTO gradeDTO = SalaryGradeResponseDTO.builder()
	                                .gradeId(grade.getGradeId())
	                                .gradeCode(grade.getGradeCode())
	                                .basicSalary(grade.getBasicSalary())
	                                .hra(grade.getHra())
	                                .da(grade.getDa())
	                                .allowances(grade.getAllowances())
	                                .pf(grade.getPf())
	                                .organizationId(grade.getOrganization() != null ? 
	                                              grade.getOrganization().getOrgId() : null)
	                                .build();
	                        
	                        dto.setSalaryGrade(gradeDTO);
	                        
	                        System.out.println("✅ " + emp.getEmpName() + " - Grade: " + grade.getGradeCode() + 
	                                         ", Basic: " + grade.getBasicSalary());
	                    } else {
	                        System.out.println("⚠️ " + emp.getEmpName() + " has NO salary grade assigned!");
	                        dto.setSalaryGrade(null);
	                    }
	                } catch (Exception e) {
	                    System.out.println("❌ Error accessing salary grade for " + emp.getEmpName() + ": " + e.getMessage());
	                    dto.setSalaryGradeId(null);
	                    dto.setGradeCode(null);
	                    dto.setSalaryGrade(null);
	                }
	                
	                return dto;
	            })
	            .collect(Collectors.toList());
	}

	@Override
	public EmployeeResponseDTO getEmployeeById(Long empId, Long departmentId, User performingUser) {
		Employee emp = employeeRepository.findById(empId).orElseThrow(() -> new RuntimeException("Employee not found"));

		if (isOrgAdmin(performingUser)) {
			Department userDept = getDepartmentForOrgAdmin(performingUser);
			if (!emp.getDepartment().equals(userDept)) {
				throw new RuntimeException("You do not have permission to view employees outside your department.");
			}
		} else {
			if (departmentId == null) {
				throw new RuntimeException("DepartmentId must be provided");
			}
			Department dept = departmentRepository.findById(departmentId)
					.orElseThrow(() -> new RuntimeException("Department not found"));
			if (!emp.getDepartment().equals(dept)) {
				throw new RuntimeException("Employee not found in the specified department");
			}
		}

		EmployeeResponseDTO dto = modelMapper.map(emp, EmployeeResponseDTO.class);
		dto.setDepartmentName(emp.getDepartment() != null ? emp.getDepartment().getName() : null);
		if (emp.getSalaryGrade() != null) {
			dto.setSalaryGradeId(emp.getSalaryGrade().getGradeId());
			dto.setGradeCode(emp.getSalaryGrade().getGradeCode());
		}
		return dto;
	}

	@Override
	@Transactional
	public EmployeeResponseDTO updateEmployee(Long empId, EmployeeRequestDTO dto, User performingUser) {

	    final Employee emp = employeeRepository.findById(empId)
	        .orElseThrow(() -> new RuntimeException("Employee not found"));

	    if (isOrgAdmin(performingUser)) {
	        Department userDept = getDepartmentForOrgAdmin(performingUser);
	        if (userDept != null && !emp.getDepartment().equals(userDept)) {
	            throw new RuntimeException("You do not have permission to update employees outside your department.");
	        }
	    }

	    if (dto.getEmpName() != null && !dto.getEmpName().isBlank()) emp.setEmpName(dto.getEmpName().trim());
	    if (dto.getEmpEmail() != null && !dto.getEmpEmail().isBlank()) emp.setEmpEmail(dto.getEmpEmail().trim());
	    if (dto.getPhone() != null && !dto.getPhone().isBlank()) emp.setPhone(dto.getPhone().trim());
	    if (dto.getBankAccountName() != null && !dto.getBankAccountName().isBlank()) emp.setBankAccountName(dto.getBankAccountName().trim());
	    if (dto.getBankAccountNo() != null && !dto.getBankAccountNo().isBlank()) emp.setBankAccountNo(dto.getBankAccountNo().trim());
	    if (dto.getIfscCode() != null && !dto.getIfscCode().isBlank()) emp.setIfscCode(dto.getIfscCode().trim());

	    if (!isOrgAdmin(performingUser) && dto.getDepartmentName() != null && !dto.getDepartmentName().isBlank()) {
	        final String deptName = dto.getDepartmentName().trim();

	        Department newDept = departmentRepository
	            .findByNameIgnoreCaseAndOrganizationAndDeletedFalse(deptName, emp.getOrganization())
	            .orElseGet(() -> {
	                Department deletedDept = departmentRepository
	                    .findByNameIgnoreCaseAndOrganizationAndDeletedTrue(deptName, emp.getOrganization())
	                    .orElse(null);

	                if (deletedDept != null) {
	                    deletedDept.setDeleted(false);
	                    return departmentRepository.save(deletedDept);
	                }

	                Department createdDept = new Department();
	                createdDept.setName(deptName);
	                createdDept.setOrganization(emp.getOrganization());
	                createdDept.setDeleted(false);
	                return departmentRepository.save(createdDept);
	            });

	        emp.setDepartment(newDept);
	    }


	    if (dto.getSalaryGradeId() != null && dto.getSalaryGradeId() > 0) {
	        SalaryGrade salaryGrade = salaryGradeRepository.findById(dto.getSalaryGradeId())
	            .orElseThrow(() -> new RuntimeException("SalaryGrade not found with id: " + dto.getSalaryGradeId()));

	        if (!salaryGrade.getOrganization().getOrgId().equals(emp.getOrganization().getOrgId())) {
	            throw new RuntimeException("Salary grade does not belong to this organization");
	        }

	        emp.setSalaryGrade(salaryGrade);
	    } else if (dto.getSalaryGradeId() != null && dto.getSalaryGradeId() == 0) {
	        emp.setSalaryGrade(null);
	    }

	    employeeRepository.save(emp);

	    auditLogService.log("UPDATE_EMPLOYEE", "EMPLOYEE", emp.getEmpId(),
	        performingUser.getUserId(), performingUser.getEmail(), ROLE_EMPLOYEE);

	    // Map response
	    EmployeeResponseDTO response = modelMapper.map(emp, EmployeeResponseDTO.class);
	    response.setDepartmentName(emp.getDepartment() != null ? emp.getDepartment().getName() : null);

	    if (emp.getSalaryGrade() != null) {
	        response.setSalaryGradeId(emp.getSalaryGrade().getGradeId());
	        response.setGradeCode(emp.getSalaryGrade().getGradeCode());
	    }

	    return response;
	}

	/**
	 * Utility method to check for non-empty strings.
	 */
	private boolean isNotBlank(String str) {
	    return str != null && !str.trim().isEmpty();
	}

	@Override
	@Transactional
	public void deleteEmployee(Long empId, User performingUser) {
		Employee emp = employeeRepository.findById(empId).orElseThrow(() -> new RuntimeException("Employee not found"));

		boolean canDelete = performingUser.getRoles().stream().anyMatch(
				role -> role.getRoleName().equals(ROLE_ORG_ADMIN) || role.getRoleName().equals(ROLE_ORGANIZATION));

		if (!canDelete) {
			throw new RuntimeException("Unauthorized: You do not have permission to delete employees.");
		}

		if (performingUser.getOrganization() == null
				|| !emp.getOrganization().getOrgId().equals(performingUser.getOrganization().getOrgId())) {
			throw new RuntimeException("Unauthorized: You can only delete employees within your own organization.");
		}

		emp.setDeleted(true);
		emp.setStatus("INACTIVE");
		employeeRepository.save(emp);

		User user = emp.getUser();
		if (user != null) {
			user.setDeleted(true);
			user.setStatus("INACTIVE");
			userRepository.save(user);
		}

		auditLogService.log("DELETE_EMPLOYEE", "EMPLOYEE", emp.getEmpId(), performingUser.getUserId(),
				performingUser.getEmail(), ROLE_EMPLOYEE);
	}

	private String generatePassword(String orgName, String empName) {
		String base = orgName.substring(0, Math.min(orgName.length(), 3)).toUpperCase()
				+ empName.substring(0, Math.min(empName.length(), 3)).toUpperCase();
		String random = Integer.toString(secureRandom.nextInt(9999));
		return base + random;
	}
	
}
