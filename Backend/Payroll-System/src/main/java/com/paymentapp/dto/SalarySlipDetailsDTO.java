package com.paymentapp.dto;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SalarySlipDetailsDTO {
    private Long slipId;
    private String period;
    private Instant generatedAt;
    private BigDecimal netAmount;

    private Long empId;
    private String empName;
    private String empEmail;
    private String phone;
    private String bankAccountNo;
    private String ifscCode;

    private String gradeCode;
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal da;
    private BigDecimal pf;
    private BigDecimal allowances;

    private Long disbursalId;
    private String disbursalPeriod;
    private String disbursalStatus;
    
    private String orgName;
    
    private String departmentName;
    private String departmentDescription;


}
