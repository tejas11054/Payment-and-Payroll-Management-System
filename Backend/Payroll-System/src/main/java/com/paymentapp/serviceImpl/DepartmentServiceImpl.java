package com.paymentapp.serviceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paymentapp.dto.DepartmentRequestDTO;
import com.paymentapp.dto.DepartmentResponseDTO;
import com.paymentapp.dto.EmployeeResponseDTO;
import com.paymentapp.entity.Department;
import com.paymentapp.entity.Employee;
import com.paymentapp.entity.Organization;
import com.paymentapp.entity.User;
import com.paymentapp.repository.DepartmentRepository;
import com.paymentapp.repository.EmployeeRepository;
import com.paymentapp.repository.OrgAdminRepository;
import com.paymentapp.repository.OrganizationRepository;
import com.paymentapp.service.AuditLogService;
import com.paymentapp.service.DepartmentService;

import lombok.RequiredArgsConstructor;
//changed
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;
    private final EmployeeRepository employeeRepository;
    private final OrgAdminRepository orgAdminRepository;  // ✅ ADD THIS
    private final ModelMapper modelMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public DepartmentResponseDTO createDepartment(Long orgId, DepartmentRequestDTO dto) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        boolean exists = departmentRepository.existsByNameAndOrganizationAndDeletedFalse(
            dto.getName().trim(), org);
        
        if (exists) {
            throw new RuntimeException("Department with name '" + dto.getName() + "' already exists in this organization");
        }

        Department department = new Department();
        department.setName(dto.getName().trim());
        department.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        department.setOrganization(org);
        department.setDeleted(false);

        Department saved = departmentRepository.save(department);

        DepartmentResponseDTO response = modelMapper.map(saved, DepartmentResponseDTO.class);
        response.setOrganizationId(org.getOrgId());
        return response;
    }

    @Override
    public DepartmentResponseDTO getDepartmentById(Long departmentId) {
        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        if (dept.isDeleted()) {
            throw new RuntimeException("Department has been deleted");
        }

        DepartmentResponseDTO response = modelMapper.map(dept, DepartmentResponseDTO.class);
        response.setOrganizationId(dept.getOrganization().getOrgId());
        return response;
    }

    @Override
    public List<DepartmentResponseDTO> getAllDepartments(Long orgId) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        return departmentRepository.findByOrganizationAndDeletedFalse(org).stream()
                .map(d -> {
                    DepartmentResponseDTO dto = modelMapper.map(d, DepartmentResponseDTO.class);
                    dto.setOrganizationId(orgId);
                    
                    // ✅ ADD: Employee count (only active employees)
                    long employeeCount = employeeRepository.countByDepartmentAndDeletedFalse(d);
                    dto.setEmployeeCount(employeeCount);
                    
                    // ✅ ADD: Admin count (optional)
                    long adminCount = orgAdminRepository.countByDepartmentAndDeletedFalse(d);
                    dto.setAdminCount(adminCount);
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public DepartmentResponseDTO getDepartmentByName(Long orgId, String name) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        Department dept = departmentRepository
                .findByNameIgnoreCaseAndOrganizationAndDeletedFalse(name.trim(), org)
                .orElseThrow(() -> new RuntimeException(
                    "Active department not found with name: " + name));

        DepartmentResponseDTO dto = modelMapper.map(dept, DepartmentResponseDTO.class);
        dto.setOrganizationId(orgId);
        return dto;
    }

    @Override
    @Transactional
    public DepartmentResponseDTO updateDepartment(Long departmentId, DepartmentRequestDTO dto) {
        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        if (dept.isDeleted()) {
            throw new RuntimeException("Cannot update a deleted department");
        }

        if (dto.getName() != null && !dto.getName().isBlank()) {
            String newName = dto.getName().trim();
            if (!dept.getName().equalsIgnoreCase(newName)) {
                boolean exists = departmentRepository.existsByNameAndOrganizationAndDeletedFalse(
                    newName, dept.getOrganization());
                
                if (exists) {
                    throw new RuntimeException("Department with name '" + newName + "' already exists");
                }
                
                dept.setName(newName);
            }
        }

        if (dto.getDescription() != null) {
            dept.setDescription(dto.getDescription().trim());
        }

        Department updated = departmentRepository.save(dept);

        DepartmentResponseDTO response = modelMapper.map(updated, DepartmentResponseDTO.class);
        response.setOrganizationId(updated.getOrganization().getOrgId());
        return response;
    }

    @Override
    @Transactional
    public void deleteDepartment(Long departmentId, User performingUser) {
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new RuntimeException("Department not found"));

        if (department.isDeleted()) {
            throw new RuntimeException("Department is already deleted");
        }

        Organization organization = department.getOrganization();
        if (!isOrganization(performingUser) && !isSameOrganization(performingUser, organization)) {
            throw new RuntimeException("Access denied");
        }

        // ✅ Check if department is in use by employees
        long employeeCount = employeeRepository.countByDepartmentAndDeletedFalse(department);
        if (employeeCount > 0) {
            throw new RuntimeException(
                "Cannot delete department: " + employeeCount + " active employees are assigned. " +
                "Please reassign or remove employees first."
            );
        }

        // ✅ Check if department is in use by org admins
        long adminCount = orgAdminRepository.countByDepartment(department);
        if (adminCount > 0) {
            throw new RuntimeException(
                "Cannot delete department: " + adminCount + " org admins are assigned. " +
                "Please reassign or remove admins first."
            );
        }

        // Soft delete
        department.setDeleted(true);
        departmentRepository.save(department);

        auditLogService.log("DELETE_DEPARTMENT", "DEPARTMENT", department.getDepartmentId(),
            performingUser.getUserId(), performingUser.getEmail(), 
            "Department soft deleted: " + department.getName());
    }

    @Override
    public List<EmployeeResponseDTO> getEmployeesByDepartment(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        if (department.isDeleted()) {
            throw new RuntimeException("Department has been deleted");
        }

        List<Employee> employees = employeeRepository.findByDepartmentAndDeletedFalse(department);

        return employees.stream()
                .map(emp -> {
                    EmployeeResponseDTO dto = modelMapper.map(emp, EmployeeResponseDTO.class);
                    dto.setDepartmentName(department.getName());
                    if (emp.getSalaryGrade() != null) {
                        dto.setSalaryGradeId(emp.getSalaryGrade().getGradeId());
                        dto.setGradeCode(emp.getSalaryGrade().getGradeCode());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Helper methods
    private boolean isOrganization(User user) {
        return user.getRoles().stream()
            .anyMatch(role -> role.getRoleName().equals("ROLE_ORGANIZATION"));
    }

    private boolean isSameOrganization(User user, Organization organization) {
        return user.getOrganization() != null && 
               user.getOrganization().getOrgId().equals(organization.getOrgId());
    }
    
    @Override
    public long getActiveEmployeeCountByDepartment(Long departmentId) {
        // ✅ Find department
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with ID: " + departmentId));
        
        // ✅ Check if department itself is deleted
        if (department.isDeleted()) {
            return 0; // Don't count employees of deleted departments
        }
        
        // ✅ Count only active employees (deleted=false) in this department
        return employeeRepository.countByDepartmentAndDeletedFalse(department);
    }
    @Override
    public long getActiveAdminCountByDepartment(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with ID: " + departmentId));
        
        if (department.isDeleted()) {
            return 0;
        }
        
        // ✅ Count only active org admins (if you have deleted flag in OrgAdmin)
        // If OrgAdmin doesn't have deleted flag, use: countByDepartment(department)
        return orgAdminRepository.countByDepartmentAndDeletedFalse(department);
    }
}
