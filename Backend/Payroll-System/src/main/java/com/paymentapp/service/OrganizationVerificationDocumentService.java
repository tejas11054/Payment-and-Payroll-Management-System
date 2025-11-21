package com.paymentapp.service;

import org.springframework.web.multipart.MultipartFile;

import com.paymentapp.entity.OrganizationVerificationDocument;

public interface OrganizationVerificationDocumentService {
    OrganizationVerificationDocument uploadVerificationDocument(Long orgId, MultipartFile file, String docType, Long uploadedByUserId);
}
