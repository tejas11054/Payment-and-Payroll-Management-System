package com.paymentapp.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.paymentapp.dto.EmployeeRequestDTO;
import com.paymentapp.dto.EmployeeResponseDTO;
import com.paymentapp.entity.User;

public interface EmployeeService {
	
	public EmployeeResponseDTO createEmployee(Long orgId, EmployeeRequestDTO dto, MultipartFile documentFile,
            User performingUser);
	
//	public List<EmployeeResponseDTO> createEmployeesBulk(Long orgId, String departmentName, Long salaryGradeId, 
//	        InputStream fileInputStream, String fileName, String fileUrl, MultipartFile documentFile, User performingUser) throws IOException
//;
	public List<EmployeeResponseDTO> createEmployeesBulk(Long orgId, String departmentName,   Long salaryGradeId, InputStream fileInputStream,
            String fileName, String fileUrl, MultipartFile documentFile, User performingUser) throws IOException;
	
	public List<EmployeeResponseDTO> getAllEmployees(Long orgId);
	
	 public EmployeeResponseDTO getEmployeeById(Long empId, Long departmentId, User performingUser);
	 
//	 public EmployeeResponseDTO updateEmployee(Long empId, EmployeeRequestDTO dto, Long departmentId, User performingUser);
	 EmployeeResponseDTO updateEmployee(Long empId, EmployeeRequestDTO dto, User performingUser);

	 
	 public void deleteEmployee(Long empId, User performingUser);
}
