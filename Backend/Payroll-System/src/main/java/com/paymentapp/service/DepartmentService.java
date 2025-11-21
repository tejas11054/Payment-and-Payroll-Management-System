package com.paymentapp.service;

import java.util.List;

import com.paymentapp.dto.DepartmentRequestDTO;
import com.paymentapp.dto.DepartmentResponseDTO;
import com.paymentapp.dto.EmployeeResponseDTO;
import com.paymentapp.entity.User;

public interface DepartmentService {
    
    /**
     * Create a new department in an organization.
     */
	   public DepartmentResponseDTO createDepartment(Long orgId, DepartmentRequestDTO dto);
    
    /**
     * Get department by ID.
     */
	public DepartmentResponseDTO getDepartmentById(Long departmentId);    
    /**
     * Get all active departments for an organization.
     */
	public List<DepartmentResponseDTO> getAllDepartments(Long orgId);
	
    /**
     * Get department by name within an organization.
     */
	public DepartmentResponseDTO getDepartmentByName(Long orgId, String name);    
    /**
     * Update an existing department.
     */
    DepartmentResponseDTO updateDepartment(Long departmentId, DepartmentRequestDTO dto);
    
    /**
     * Soft delete a department (sets deleted flag to true).
     */
    void deleteDepartment(Long departmentId, User performingUser);
    
    /**
     * Get all employees in a specific department.
     */
    public List<EmployeeResponseDTO> getEmployeesByDepartment(Long departmentId);
 long getActiveEmployeeCountByDepartment(Long departmentId);
    

    long getActiveAdminCountByDepartment(Long departmentId);
    
}
