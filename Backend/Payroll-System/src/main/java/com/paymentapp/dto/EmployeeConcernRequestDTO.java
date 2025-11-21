package com.paymentapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeConcernRequestDTO {

    @NotBlank(message = "Description is required")
    private String description;

    private String attachmentUrl;

    @NotNull(message = "Employee ID is required")
    private Long empid;

}
