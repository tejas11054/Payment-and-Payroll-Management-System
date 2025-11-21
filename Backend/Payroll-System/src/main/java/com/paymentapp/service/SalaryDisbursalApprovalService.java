package com.paymentapp.service;

import java.util.List;

import com.paymentapp.dto.SalaryDisbursalApprovalRequestDTO;
import com.paymentapp.dto.SalaryDisbursalRequestDTO;

public interface SalaryDisbursalApprovalService {
	 public void processApproval(SalaryDisbursalApprovalRequestDTO approvalRequest);
	 List<SalaryDisbursalRequestDTO> getPendingRequests();
	 SalaryDisbursalRequestDTO getRequestDetails(Long disbursalId);
}
