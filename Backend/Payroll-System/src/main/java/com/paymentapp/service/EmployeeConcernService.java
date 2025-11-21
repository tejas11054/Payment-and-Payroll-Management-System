package com.paymentapp.service;

import org.springframework.web.multipart.MultipartFile;

import com.paymentapp.dto.EmployeeConcernRequestDTO;
import com.paymentapp.dto.EmployeeConcernResponseDTO;

public interface EmployeeConcernService {
	
    EmployeeConcernResponseDTO raiseConcern(EmployeeConcernRequestDTO request, MultipartFile file);
    
    EmployeeConcernResponseDTO solveConcern(Long concernId, Long resolvedByUserId, String responseText);
    
    long countPendingConcernsByEmployee(Long employeeId);

}
