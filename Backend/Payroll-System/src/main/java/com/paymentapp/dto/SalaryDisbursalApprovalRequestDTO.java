package com.paymentapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SalaryDisbursalApprovalRequestDTO {
    
    @NotNull
    private Long disbursalRequestId;

    @NotBlank
    private String action; 

    private String comment;
    
}
