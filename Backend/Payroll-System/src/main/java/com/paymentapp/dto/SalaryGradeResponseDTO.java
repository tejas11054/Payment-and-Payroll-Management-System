package com.paymentapp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryGradeResponseDTO {

    private Long gradeId;
    private String gradeCode;
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal da;
    private BigDecimal pf;
    private BigDecimal allowances;
    private Long organizationId;
}
