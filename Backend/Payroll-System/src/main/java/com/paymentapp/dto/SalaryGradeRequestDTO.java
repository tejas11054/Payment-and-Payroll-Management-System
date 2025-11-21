package com.paymentapp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.*;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryGradeRequestDTO {

    @NotBlank(message = "Grade code is required")
    private String gradeCode;

    @NotNull(message = "Basic salary is required")
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal basicSalary;

    @NotNull(message = "HRA is required")
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal hra;

    @NotNull(message = "DA is required")
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal da;

    @NotNull(message = "PF is required")
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal pf;

    @NotNull(message = "Allowances are required")
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal allowances;

}
