package com.paymentapp.service;

import com.paymentapp.dto.SalaryDisbursalRequestCreateDTO;
import com.paymentapp.dto.SalaryDisbursalRequestDTO;

public interface SalaryDisbursalService {
    
	public SalaryDisbursalRequestDTO createCustomSalaryDisbursal(SalaryDisbursalRequestCreateDTO dto);
	
}
