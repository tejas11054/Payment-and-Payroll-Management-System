// SalaryDisbursalPaymentGroupDTO.java
package com.paymentapp.dto;

import java.util.List;

import lombok.Data;

@Data
public class SalaryDisbursalPaymentGroupDTO {
    private String type;  
    private List<Long> ids; 
}
