package com.paymentapp.serviceImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paymentapp.dto.SalaryDisbursalLineDTO;
import com.paymentapp.dto.SalaryDisbursalPaymentGroupDTO;
import com.paymentapp.dto.SalaryDisbursalRequestCreateDTO;
import com.paymentapp.dto.SalaryDisbursalRequestDTO;
import com.paymentapp.entity.Employee;
import com.paymentapp.entity.OrgAdmin;
import com.paymentapp.entity.Organization;
import com.paymentapp.entity.SalaryDisbursalLine;
import com.paymentapp.entity.SalaryDisbursalRequest;
import com.paymentapp.entity.SalaryGrade;
import com.paymentapp.entity.User;
import com.paymentapp.exception.DuplicatePayrollException;
import com.paymentapp.repository.EmployeeRepository;
import com.paymentapp.repository.OrgAdminRepository;
import com.paymentapp.repository.OrganizationRepository;
import com.paymentapp.repository.SalaryDisbursalRequestRepository;
import com.paymentapp.repository.SalaryGradeRepository;
import com.paymentapp.repository.UserRepository;
import com.paymentapp.service.AuditLogService;
import com.paymentapp.service.SalaryDisbursalService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SalaryDisbursalServiceImpl implements SalaryDisbursalService {

    private final EmployeeRepository employeeRepository;
    private final OrgAdminRepository orgAdminRepository;
    private final SalaryGradeRepository salaryGradeRepo;
    private final SalaryDisbursalRequestRepository disbursalRequestRepo;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public SalaryDisbursalRequestDTO createCustomSalaryDisbursal(SalaryDisbursalRequestCreateDTO dto) {
        
        System.out.println("\n\n");
        System.out.println("📋 ════════════════════════════════════════");
        System.out.println("📋 CREATING SALARY DISBURSAL REQUEST");
        System.out.println("📋 ════════════════════════════════════════");
        System.out.println("   Organization ID: " + dto.getOrgId());
        System.out.println("   Period: " + dto.getPeriod());
        System.out.println("   Remarks: " + dto.getRemarks());
        
        // ✅ STEP 1: Fetch organization
        Organization org = organizationRepository.findById(dto.getOrgId())
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + dto.getOrgId()));
        
        System.out.println("✅ Organization found: " + org.getOrgName());

        // ✅ STEP 2: CHECK FOR DUPLICATE PERIOD (FIXED)
        System.out.println("\n🔍 Checking for existing payroll in period: " + dto.getPeriod());
        
        List<SalaryDisbursalRequest> existingRequests = disbursalRequestRepo
                .findByOrganization_OrgIdAndPeriodAndStatusIn(
                    dto.getOrgId(), 
                    dto.getPeriod(), 
                    Arrays.asList("PENDING", "APPROVED")
                );
        
        System.out.println("   Found " + existingRequests.size() + " existing request(s)");
        
        if (!existingRequests.isEmpty()) {
            SalaryDisbursalRequest existing = existingRequests.get(0);
            
            String errorMsg = String.format(
                "Salary disbursal request already exists for period %s with status: %s. " +
                "Please wait for approval or contact administrator.",
                dto.getPeriod(),
                existing.getStatus()
            );
            
            System.err.println("❌ DUPLICATE REQUEST DETECTED!");
            System.err.println("   Existing Request ID: " + existing.getDisbursalId());
            System.err.println("   Status: " + existing.getStatus());
            System.err.println("   Total Amount: ₹" + existing.getTotalAmount());
            System.err.println("   Created At: " + existing.getCreatedAt());
            System.err.println("\n" + errorMsg);
            
            throw new DuplicatePayrollException(dto.getPeriod(), existing.getStatus());
        }
        
        System.out.println("✅ No existing request found. Proceeding with creation...\n");

        // ✅ STEP 3: Create new request
        SalaryDisbursalRequest request = new SalaryDisbursalRequest();
        request.setOrganization(org);
        request.setPeriod(dto.getPeriod());
        request.setStatus("PENDING");
        request.setRemarks(dto.getRemarks());
        request.setTotalAmount(BigDecimal.ZERO);

        List<SalaryDisbursalLine> lines = new ArrayList<>();
        BigDecimal totalNet = BigDecimal.ZERO;
        
        int employeeCount = 0;
        int orgAdminCount = 0;

        // ✅ STEP 4: Process payments
        System.out.println("💰 Processing payments...");
        
        for (SalaryDisbursalPaymentGroupDTO group : dto.getPayments()) {
            String paymentType = group.getType();
            System.out.println("\n   Payment Group: " + paymentType);
            System.out.println("   IDs: " + group.getIds());

            if ("ROLE_EMPLOYEE".equalsIgnoreCase(paymentType)) {
                List<Employee> employees = employeeRepository.findAllById(group.getIds());
                System.out.println("   Found " + employees.size() + " employee(s)");

                for (Employee emp : employees) {
                    SalaryGrade grade = emp.getSalaryGrade();
                    if (grade == null) {
                        String error = "Salary grade missing for employee: " + emp.getEmpName() + " (ID: " + emp.getEmpId() + ")";
                        System.err.println("❌ " + error);
                        throw new RuntimeException(error);
                    }

                    BigDecimal basic = grade.getBasicSalary() != null ? grade.getBasicSalary() : BigDecimal.ZERO;
                    BigDecimal hra = grade.getHra() != null ? grade.getHra() : BigDecimal.ZERO;
                    BigDecimal da = grade.getDa() != null ? grade.getDa() : BigDecimal.ZERO;
                    BigDecimal allowances = grade.getAllowances() != null ? grade.getAllowances() : BigDecimal.ZERO;
                    BigDecimal pf = grade.getPf() != null ? grade.getPf() : BigDecimal.ZERO;

                    BigDecimal gross = basic.add(hra).add(da).add(allowances);
                    BigDecimal deductions = pf;
                    BigDecimal net = gross.subtract(deductions);

                    SalaryDisbursalLine line = new SalaryDisbursalLine();
                    line.setEmployee(emp);
                    line.setGrossSalary(gross);
                    line.setDeductions(deductions);
                    line.setNetAmount(net);
                    line.setStatus("PENDING");
                    line.setDisbursalRequest(request);

                    lines.add(line);
                    totalNet = totalNet.add(net);
                    employeeCount++;
                    
                    System.out.println("      ✅ " + emp.getEmpName() + " - ₹" + net);
                }

            } else if ("ROLE_ORG_ADMIN".equalsIgnoreCase(paymentType)) {
                List<OrgAdmin> admins = orgAdminRepository.findAllById(group.getIds());
                System.out.println("   Found " + admins.size() + " org admin(s)");

                for (OrgAdmin admin : admins) {
                    SalaryGrade grade = admin.getSalaryGrade();
                    if (grade == null) {
                        String error = "Salary grade missing for Org Admin: " + admin.getName() + " (ID: " + admin.getOrgAdminId() + ")";
                        System.err.println("❌ " + error);
                        throw new RuntimeException(error);
                    }

                    BigDecimal basic = grade.getBasicSalary() != null ? grade.getBasicSalary() : BigDecimal.ZERO;
                    BigDecimal hra = grade.getHra() != null ? grade.getHra() : BigDecimal.ZERO;
                    BigDecimal da = grade.getDa() != null ? grade.getDa() : BigDecimal.ZERO;
                    BigDecimal allowances = grade.getAllowances() != null ? grade.getAllowances() : BigDecimal.ZERO;
                    BigDecimal pf = grade.getPf() != null ? grade.getPf() : BigDecimal.ZERO;

                    BigDecimal gross = basic.add(hra).add(da).add(allowances);
                    BigDecimal deductions = pf;
                    BigDecimal net = gross.subtract(deductions);

                    SalaryDisbursalLine line = new SalaryDisbursalLine();
                    line.setOrgAdmin(admin);
                    line.setGrossSalary(gross);
                    line.setDeductions(deductions);
                    line.setNetAmount(net);
                    line.setStatus("PENDING");
                    line.setDisbursalRequest(request);

                    lines.add(line);
                    totalNet = totalNet.add(net);
                    orgAdminCount++;
                    
                    System.out.println("      ✅ " + admin.getName() + " - ₹" + net);
                }

            } else {
                String error = "Unsupported payment type: " + paymentType;
                System.err.println("❌ " + error);
                throw new RuntimeException(error);
            }
        }

        // ✅ Validate that at least one person selected
        if (lines.isEmpty()) {
            String error = "No employees or admins selected for salary disbursal";
            System.err.println("❌ " + error);
            throw new RuntimeException(error);
        }

        request.setLines(lines);
        request.setTotalAmount(totalNet);

        // ✅ STEP 5: Save request
        SalaryDisbursalRequest savedRequest = disbursalRequestRepo.save(request);
        
        System.out.println("\n📊 ════════════════════════════════════════");
        System.out.println("📊 SALARY DISBURSAL REQUEST CREATED");
        System.out.println("📊 ════════════════════════════════════════");
        System.out.println("   Request ID: " + savedRequest.getDisbursalId());
        System.out.println("   Organization: " + org.getOrgName());
        System.out.println("   Period: " + savedRequest.getPeriod());
        System.out.println("   Status: " + savedRequest.getStatus());
        System.out.println("   Total Employees: " + employeeCount);
        System.out.println("   Total Org Admins: " + orgAdminCount);
        System.out.println("   Total People: " + (employeeCount + orgAdminCount));
        System.out.println("   Total Amount: ₹" + totalNet);
        System.out.println("📊 ════════════════════════════════════════\n\n");

        // ✅ STEP 6: Audit logging
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found for audit logging"));

        auditLogService.log(
                "CREATED_SALARY_DISBURSAL",
                "SalaryDisbursalRequest",
                savedRequest.getDisbursalId(),
                user.getUserId(),
                user.getEmail(),
                getFirstRole(user)
        );

        return mapToDTO(savedRequest);
    }

    private SalaryDisbursalRequestDTO mapToDTO(SalaryDisbursalRequest request) {
        SalaryDisbursalRequestDTO dto = new SalaryDisbursalRequestDTO();
        dto.setDisbursalId(request.getDisbursalId());
        dto.setOrgId(request.getOrganization().getOrgId());
        dto.setOrgName(request.getOrganization().getOrgName());
        dto.setPeriod(request.getPeriod());
        dto.setStatus(request.getStatus());
        dto.setTotalAmount(request.getTotalAmount());
        dto.setRemarks(request.getRemarks());
        dto.setCreatedAt(request.getCreatedAt());

        List<SalaryDisbursalLineDTO> lineDTOs = new ArrayList<>();
        for (SalaryDisbursalLine line : request.getLines()) {
            SalaryDisbursalLineDTO lineDTO = new SalaryDisbursalLineDTO();
            lineDTO.setLineId(line.getLineId());

            if (line.getEmployee() != null) {
                lineDTO.setEmployeeName(line.getEmployee().getEmpName());
                lineDTO.setEmployeeEmail(line.getEmployee().getEmpEmail());
            } else if (line.getOrgAdmin() != null) {
                lineDTO.setEmployeeName(line.getOrgAdmin().getName());
                lineDTO.setEmployeeEmail(line.getOrgAdmin().getEmail());
            }

            lineDTO.setGrossSalary(line.getGrossSalary());
            lineDTO.setDeductions(line.getDeductions());
            lineDTO.setNetAmount(line.getNetAmount());
            lineDTO.setStatus(line.getStatus());

            lineDTOs.add(lineDTO);
        }

        dto.setLines(lineDTOs);

        return dto;
    }

    private String getFirstRole(User user) {
        return user.getRoles().stream()
                .findFirst()
                .map(role -> role.getRoleName())
                .orElse("UNKNOWN");
    }
}
