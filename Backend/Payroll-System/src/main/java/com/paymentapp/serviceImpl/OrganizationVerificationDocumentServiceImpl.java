package com.paymentapp.serviceImpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.paymentapp.entity.Organization;
import com.paymentapp.entity.OrganizationVerificationDocument;
import com.paymentapp.repository.OrganizationRepository;
import com.paymentapp.repository.OrganizationVerificationDocumentRepository;
import com.paymentapp.service.OrganizationVerificationDocumentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationVerificationDocumentServiceImpl implements OrganizationVerificationDocumentService {

    private final CloudinaryService cloudinaryService;
    private final OrganizationVerificationDocumentRepository orgDocRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    @Transactional
    public OrganizationVerificationDocument uploadVerificationDocument(Long orgId, MultipartFile file, String docType, Long uploadedByUserId) {

        Organization organization = organizationRepository.findById(orgId)
            .orElseThrow(() -> new RuntimeException("Organization not found with id: " + orgId));

        String cloudUrl = cloudinaryService.uploadFile(file, orgId);

        OrganizationVerificationDocument orgDoc = new OrganizationVerificationDocument();
        orgDoc.setOrganization(organization);
        orgDoc.setFilename(file.getOriginalFilename());
        orgDoc.setCloudUrl(cloudUrl);
        orgDoc.setDocType(docType);
        orgDoc.setUploadedByUserId(uploadedByUserId);
        orgDoc.setStatus("UPLOADED"); 

        return orgDocRepository.save(orgDoc);
    }
}
