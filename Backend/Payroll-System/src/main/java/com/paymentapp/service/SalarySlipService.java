package com.paymentapp.service;

import com.paymentapp.dto.SalarySlipDetailsDTO;

public interface SalarySlipService {
    SalarySlipDetailsDTO getSalarySlipDetails(Long slipId);

	byte[] generateSalarySlipPDF(Long slipId) throws Exception;
}
