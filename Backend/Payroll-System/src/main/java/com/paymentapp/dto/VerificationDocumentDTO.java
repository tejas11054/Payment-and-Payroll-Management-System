package com.paymentapp.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationDocumentDTO {
    private Long docId;
    private String filename;
    private String cloudUrl;
    private String docType;
    private String status;
    private Instant uploadedAt;
    private Long uploadedByUserId;
}
