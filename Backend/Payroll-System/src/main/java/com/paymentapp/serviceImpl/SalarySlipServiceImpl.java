package com.paymentapp.serviceImpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paymentapp.dto.SalarySlipDetailsDTO;
import com.paymentapp.entity.Department;
import com.paymentapp.entity.Employee;
import com.paymentapp.entity.Organization;
import com.paymentapp.entity.SalaryDisbursalRequest;
import com.paymentapp.entity.SalaryGrade;
import com.paymentapp.entity.SalarySlip;
import com.paymentapp.repository.SalarySlipRepository;
import com.paymentapp.service.SalarySlipService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SalarySlipServiceImpl implements SalarySlipService {

    private final SalarySlipRepository salarySlipRepository;
    private final SalarySlipPDFGenerator pdfGenerator;
    @Override
    @Transactional(readOnly = true)
    public SalarySlipDetailsDTO getSalarySlipDetails(Long slipId) {
        SalarySlip slip = salarySlipRepository.findById(slipId)
                .orElseThrow(() -> new RuntimeException("Salary slip not found with id: " + slipId));

        SalaryDisbursalRequest disbursal = slip.getDisbursal();

        SalarySlipDetailsDTO.SalarySlipDetailsDTOBuilder dtoBuilder = SalarySlipDetailsDTO.builder()
                .slipId(slip.getSlipId())
                .period(slip.getPeriod())
                .generatedAt(slip.getGeneratedAt())
                .netAmount(slip.getNetAmount())
                .disbursalId(disbursal.getDisbursalId())
                .disbursalPeriod(disbursal.getPeriod())
                .disbursalStatus(disbursal.getStatus());

        // If it's an Employee
        if (slip.getEmployee() != null) {
            Employee emp = slip.getEmployee();
            Organization org = emp.getOrganization();
            Department dept = emp.getDepartment();
            SalaryGrade grade = emp.getSalaryGrade();

            dtoBuilder
                    .empId(emp.getEmpId())
                    .empName(emp.getEmpName())
                    .empEmail(emp.getEmpEmail())
                    .phone(emp.getPhone())
                    .bankAccountNo(emp.getBankAccountNo())
                    .ifscCode(emp.getIfscCode())
                    .orgName(org != null ? org.getOrgName() : null)
                    .departmentName(dept != null ? dept.getName() : null)
                    .departmentDescription(dept != null ? dept.getDescription() : null)
                    .gradeCode(grade != null ? grade.getGradeCode() : null)
                    .basicSalary(grade != null ? grade.getBasicSalary() : null)
                    .hra(grade != null ? grade.getHra() : null)
                    .da(grade != null ? grade.getDa() : null)
                    .pf(grade != null ? grade.getPf() : null)
                    .allowances(grade != null ? grade.getAllowances() : null);

        }
        // If it's an OrgAdmin
        else if (slip.getOrgAdmin() != null) {
            var admin = slip.getOrgAdmin();
            Organization org = admin.getOrganization();
            SalaryGrade grade = admin.getSalaryGrade();
            Department dept = admin.getDepartment();

            dtoBuilder
                    .empId(admin.getOrgAdminId()) // or create a separate field like adminId
                    .empName(admin.getName())
                    .empEmail(admin.getEmail())
                    .phone(admin.getPhone())
                    .bankAccountNo(admin.getBankAccountNo())
                    .ifscCode(admin.getIfscCode())
                    .orgName(org != null ? org.getOrgName() : null)
                    .departmentName(dept != null ? dept.getName() : null)
                    .departmentDescription(dept != null ? dept.getDescription() : null)
                    .gradeCode(grade != null ? grade.getGradeCode() : null)
                    .basicSalary(grade != null ? grade.getBasicSalary() : null)
                    .hra(grade != null ? grade.getHra() : null)
                    .da(grade != null ? grade.getDa() : null)
                    .pf(grade != null ? grade.getPf() : null)
                    .allowances(grade != null ? grade.getAllowances() : null);
        }


        return dtoBuilder.build();
    }

    @Override
    public byte[] generateSalarySlipPDF(Long slipId) throws Exception {
        SalarySlipDetailsDTO slip = getSalarySlipDetails(slipId);
        return pdfGenerator.generateSalarySlipPDF(slip);
    }
}
