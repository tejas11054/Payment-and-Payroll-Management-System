package com.paymentapp.dto;

import lombok.Data;

@Data
public class EmployeeConcernSolveRequestDTO {
    private Long resolvedByOrgAdminId;  
    private String responseText;       
}
