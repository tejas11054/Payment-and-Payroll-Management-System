package com.paymentapp.dto;

import java.util.List;

import lombok.Data;

@Data
public class SalaryDisbursalRequestCreateDTO {
    private Long orgId;
    private String period;
    private String remarks;
    private List<SalaryDisbursalPaymentGroupDTO> payments;
}
