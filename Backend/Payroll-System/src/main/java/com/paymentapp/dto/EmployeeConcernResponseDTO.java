package com.paymentapp.dto;

import java.time.Instant;

import lombok.Data;

@Data
public class EmployeeConcernResponseDTO {

    private Long concernId;
    private String description;
    private String attachmentUrl;
    private String status;
    private Instant raisedAt;
    private Long empid;
}
