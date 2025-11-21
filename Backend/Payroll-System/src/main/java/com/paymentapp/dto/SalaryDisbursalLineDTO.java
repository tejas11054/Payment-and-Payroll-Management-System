package com.paymentapp.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalaryDisbursalLineDTO {
    private Long lineId;
    private String employeeName;
    private String employeeEmail;
    private BigDecimal grossSalary;
    private BigDecimal deductions;
    private BigDecimal netAmount;
    private String status;
}

