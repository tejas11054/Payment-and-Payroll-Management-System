package com.paymentapp.serviceImpl;

import java.time.Instant;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.paymentapp.dto.EmployeeConcernRequestDTO;
import com.paymentapp.dto.EmployeeConcernResponseDTO;
import com.paymentapp.entity.Employee;
import com.paymentapp.entity.EmployeeConcern;
import com.paymentapp.entity.Organization;
import com.paymentapp.repository.EmployeeConcernRepository;
import com.paymentapp.repository.EmployeeRepository;
import com.paymentapp.repository.OrgAdminRepository;
import com.paymentapp.service.EmployeeConcernService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeConcernServiceImpl implements EmployeeConcernService {

    private final EmployeeConcernRepository concernRepository;
    private final EmployeeRepository employeeRepository;
    private final Cloudinary cloudinary;
    private final OrgAdminRepository orgAdminRepository;

    @Override
    public EmployeeConcernResponseDTO raiseConcern(EmployeeConcernRequestDTO request, MultipartFile file) {
        Employee employee = employeeRepository.findById(request.getEmpid())
            .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + request.getEmpid()));

        Organization organization = employee.getOrganization();
        if (organization == null) {
            throw new IllegalStateException("Employee does not belong to any organization");
        }

        String attachmentUrl = null;

        if (file != null && !file.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
                attachmentUrl = (String) uploadResult.get("secure_url");
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload file to Cloudinary", e);
            }
        }

        EmployeeConcern concern = EmployeeConcern.builder()
                .description(request.getDescription())
                .attachmentUrl(attachmentUrl)
                .status("IN_PROGRESS")
                .employee(employee)
                .organization(organization)
                .build();

        EmployeeConcern savedConcern = concernRepository.save(concern);

        return mapToResponse(savedConcern);
    }

    private EmployeeConcernResponseDTO mapToResponse(EmployeeConcern concern) {
        EmployeeConcernResponseDTO response = new EmployeeConcernResponseDTO();
        response.setConcernId(concern.getConcernId());
        response.setDescription(concern.getDescription());
        response.setAttachmentUrl(concern.getAttachmentUrl());
        response.setStatus(concern.getStatus());
        response.setRaisedAt(concern.getRaisedAt());
        response.setEmpid(concern.getEmployee() != null ? concern.getEmployee().getEmpId() : null);
        return response;
    }

    @Override
    public EmployeeConcernResponseDTO solveConcern(Long concernId, Long resolvedByOrgAdminId, String responseText) {
        EmployeeConcern concern = concernRepository.findById(concernId)
            .orElseThrow(() -> new IllegalArgumentException("Concern not found with id: " + concernId));

        boolean orgAdminExists = orgAdminRepository.existsById(resolvedByOrgAdminId);
        if (!orgAdminExists) {
            throw new IllegalArgumentException("Organization Admin not found with id: " + resolvedByOrgAdminId);
        }

        concern.setStatus("RESOLVED");
        concern.setResolvedByUserId(resolvedByOrgAdminId); 
        concern.setResponseText(responseText);
        concern.setResolvedAt(Instant.now());

        EmployeeConcern updatedConcern = concernRepository.save(concern);

        return mapToResponse(updatedConcern);
    }
    
    @Override
    public long countPendingConcernsByEmployee(Long employeeId) {
        return concernRepository.countByEmployeeIdAndStatus(employeeId, "PENDING");
    }

}

