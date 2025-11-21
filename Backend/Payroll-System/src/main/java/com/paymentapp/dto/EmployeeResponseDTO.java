package com.paymentapp.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class EmployeeResponseDTO {
    private Long empId;
    private String empName;
    private String empEmail;
    private String phone;
    private String bankAccountNo;
    private String ifscCode;
    private String status;
    private Long organizationId;
    private Long departmentId;
    private String departmentName;
    private Long salaryGradeId;
    private String gradeCode;
    private String bankAccountName;
    private SalaryGradeResponseDTO salaryGrade;
}
