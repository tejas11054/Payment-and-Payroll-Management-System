package com.paymentapp.serviceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paymentapp.dto.SalaryGradeRequestDTO;
import com.paymentapp.dto.SalaryGradeResponseDTO;
import com.paymentapp.entity.Organization;
import com.paymentapp.entity.SalaryGrade;
import com.paymentapp.entity.User;
import com.paymentapp.exception.ResourceNotFoundException;
import com.paymentapp.repository.OrganizationRepository;
import com.paymentapp.repository.SalaryGradeRepository;
import com.paymentapp.repository.UserRepository;
import com.paymentapp.service.AuditLogService;
import com.paymentapp.service.SalaryGradeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SalaryGradeServiceImpl implements SalaryGradeService {

    private final SalaryGradeRepository salaryGradeRepository;
    private final OrganizationRepository organizationRepository;
    private final ModelMapper modelMapper;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    @Override
    public SalaryGradeResponseDTO createSalaryGrade(Long orgId, SalaryGradeRequestDTO requestDTO) {
        Organization organization = organizationRepository.findById(orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + orgId));

        SalaryGrade salaryGrade = modelMapper.map(requestDTO, SalaryGrade.class);
        salaryGrade.setOrganization(organization);
        salaryGrade.setDeleted(false);
        
        SalaryGrade saved = salaryGradeRepository.save(salaryGrade);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        String role = user.getRoles().stream().findFirst().map(r -> r.getRoleName()).orElse("UNKNOWN");

        auditLogService.log(
            "CREATE_SALARY_GRADE",
            "SalaryGrade",
            saved.getGradeId(),
            user.getUserId(),
            user.getEmail(),
            role
        );

        return modelMapper.map(saved, SalaryGradeResponseDTO.class);
    }

    @Override
    public SalaryGradeResponseDTO updateSalaryGrade(Long orgId, Long gradeId, SalaryGradeRequestDTO requestDTO) {
        Organization organization = organizationRepository.findById(orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + orgId));

        SalaryGrade existing = salaryGradeRepository.findById(gradeId)
            .orElseThrow(() -> new ResourceNotFoundException("SalaryGrade not found with id: " + gradeId));

        if (!existing.getOrganization().getOrgId().equals(orgId)) {
            throw new IllegalArgumentException("SalaryGrade does not belong to the organization");
        }

        modelMapper.map(requestDTO, existing);
        existing.setOrganization(organization);

        SalaryGrade updated = salaryGradeRepository.save(existing);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        String role = user.getRoles().stream().findFirst().map(r -> r.getRoleName()).orElse("UNKNOWN");

        auditLogService.log(
            "UPDATE_SALARY_GRADE",
            "SalaryGrade",
            updated.getGradeId(),
            user.getUserId(),
            user.getEmail(),
            role
        );

        return modelMapper.map(updated, SalaryGradeResponseDTO.class);
    }

    @Override
    public SalaryGradeResponseDTO getSalaryGradeById(Long orgId, Long gradeId) {
        SalaryGrade salaryGrade = salaryGradeRepository.findById(gradeId)
            .orElseThrow(() -> new ResourceNotFoundException("SalaryGrade not found with id: " + gradeId));

        if (!salaryGrade.getOrganization().getOrgId().equals(orgId)) {
            throw new IllegalArgumentException("SalaryGrade does not belong to the organization");
        }

        return modelMapper.map(salaryGrade, SalaryGradeResponseDTO.class);
    }

    @Override
    public List<SalaryGradeResponseDTO> getAllSalaryGrades(Long orgId) {
        Organization organization = organizationRepository.findById(orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + orgId));

        List<SalaryGrade> grades = salaryGradeRepository.findByOrganization(organization);
        return grades.stream()
                .map(grade -> modelMapper.map(grade, SalaryGradeResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSalaryGrade(Long orgId, Long gradeId) {
        SalaryGrade salaryGrade = salaryGradeRepository.findById(gradeId)
            .orElseThrow(() -> new ResourceNotFoundException("SalaryGrade not found with id: " + gradeId));

        if (!salaryGrade.getOrganization().getOrgId().equals(orgId)) {
            throw new IllegalArgumentException("SalaryGrade does not belong to the organization");
        }

        salaryGrade.setDeleted(true);
        salaryGradeRepository.save(salaryGrade);


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        String role = user.getRoles().stream().findFirst().map(r -> r.getRoleName()).orElse("UNKNOWN");

        auditLogService.log(
            "DELETE_SALARY_GRADE",
            "SalaryGrade",
            salaryGrade.getGradeId(),
            user.getUserId(),
            user.getEmail(),
            role
        );
    }
}
