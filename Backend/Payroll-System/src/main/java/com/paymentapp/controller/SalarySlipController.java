package com.paymentapp.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paymentapp.dto.SalarySlipDetailsDTO;
import com.paymentapp.entity.Employee;
import com.paymentapp.entity.SalaryDisbursalRequest;
import com.paymentapp.entity.SalaryGrade;
import com.paymentapp.entity.SalarySlip;
import com.paymentapp.repository.SalarySlipRepository;
import com.paymentapp.service.SalarySlipService;
import com.paymentapp.serviceImpl.SalarySlipPDFGenerator;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/salary-slip")
@RequiredArgsConstructor
public class SalarySlipController {

    private final SalarySlipService salarySlipService;
    private final SalarySlipRepository salarySlipRepository;
    private final SalarySlipPDFGenerator pdfGenerator;

    @GetMapping("/{id}")
    public ResponseEntity<SalarySlipDetailsDTO> getSalarySlip(@PathVariable("id") Long slipId) {
        SalarySlipDetailsDTO dto = salarySlipService.getSalarySlipDetails(slipId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/employee/{empId}")
    public ResponseEntity<List<SalarySlipDetailsDTO>> getEmployeeSalarySlips(
            @PathVariable Long empId) {
        
        System.out.println("\n\n");
        System.out.println(" ════════════════════════════════════════");
        System.out.println(" GETTING SALARY SLIPS FOR EMPLOYEE");
        System.out.println(" ════════════════════════════════════════");
        System.out.println("   Employee ID: " + empId);
        
        try {
            List<SalarySlip> slips = salarySlipRepository
                    .findByEmployee_EmpIdOrderByGeneratedAtDesc(empId);
            
            System.out.println(" Found " + slips.size() + " salary slip(s)");
            
            List<SalarySlipDetailsDTO> dtos = slips.stream()
                    .map(slip -> {
                        System.out.println("\n   Processing slip ID: " + slip.getSlipId());
                        
                        SalarySlipDetailsDTO.SalarySlipDetailsDTOBuilder builder = SalarySlipDetailsDTO.builder()
                                .slipId(slip.getSlipId())
                                .period(slip.getPeriod())
                                .generatedAt(slip.getGeneratedAt())
                                .netAmount(slip.getNetAmount());
                        
                        if (slip.getEmployee() != null) {
                            Employee emp = slip.getEmployee();
                            System.out.println("      Employee: " + emp.getEmpName());
                            
                            builder.empId(emp.getEmpId())
                                   .empName(emp.getEmpName())
                                   .empEmail(emp.getEmpEmail())
                                   .phone(emp.getPhone())
                                   .bankAccountNo(emp.getBankAccountNo())
                                   .ifscCode(emp.getIfscCode());
                            
                            if (emp.getSalaryGrade() != null) {
                                SalaryGrade grade = emp.getSalaryGrade();
                                System.out.println("      Grade: " + grade.getGradeCode());
                                
                                builder.gradeCode(grade.getGradeCode())
                                       .basicSalary(grade.getBasicSalary())
                                       .hra(grade.getHra())
                                       .da(grade.getDa())
                                       .pf(grade.getPf())
                                       .allowances(grade.getAllowances());
                            }
                            
                            if (emp.getOrganization() != null) {
                                builder.orgName(emp.getOrganization().getOrgName());
                                System.out.println("      Organization: " + emp.getOrganization().getOrgName());
                            }
                            
                            if (emp.getDepartment() != null) {
                                builder.departmentName(emp.getDepartment().getName())
                                       .departmentDescription(emp.getDepartment().getDescription());
                                System.out.println("      Department: " + emp.getDepartment().getName());
                            }
                        }
                        
                        if (slip.getDisbursalLine() != null && 
                            slip.getDisbursalLine().getDisbursalRequest() != null) {
                            SalaryDisbursalRequest disbursal = slip.getDisbursalLine().getDisbursalRequest();
                            
                            builder.disbursalId(disbursal.getDisbursalId())
                                   .disbursalPeriod(disbursal.getPeriod())
                                   .disbursalStatus(disbursal.getStatus());
                            
                            System.out.println("      Disbursal ID: " + disbursal.getDisbursalId());
                        }
                        
                        return builder.build();
                    })
                    .collect(Collectors.toList());
            
            System.out.println("\n Successfully mapped " + dtos.size() + " salary slip(s) to DTOs");
            System.out.println(" ════════════════════════════════════════\n\n");
            
            return ResponseEntity.ok(dtos);
            
        } catch (Exception e) {
            System.err.println(" ERROR getting salary slips:");
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
            
            System.err.println(" ════════════════════════════════════════\n\n");
            
            throw new RuntimeException("Failed to fetch salary slips: " + e.getMessage());
        }

    }
    

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadSalarySlip(@PathVariable("id") Long slipId) {
        try {
            System.out.println("\n ════════════════════════════════════════");
            System.out.println(" DOWNLOADING SALARY SLIP PDF");
            System.out.println(" ════════════════════════════════════════");
            System.out.println("   Slip ID: " + slipId);
            
            SalarySlipDetailsDTO slip = salarySlipService.getSalarySlipDetails(slipId);
            
            System.out.println(" Salary slip data retrieved");
            System.out.println("   Employee: " + slip.getEmpName());
            System.out.println("   Period: " + slip.getPeriod());
            
            byte[] pdfBytes = pdfGenerator.generateSalarySlipPDF(slip);
            
            System.out.println(" PDF generated successfully");
            System.out.println("   Size: " + pdfBytes.length + " bytes");
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData(
                "attachment", 
                "salary-slip-" + slip.getPeriod() + "-" + slipId + ".pdf"
            );
            headers.setContentLength(pdfBytes.length);
            
            System.out.println(" Download ready!");
            System.out.println(" ════════════════════════════════════════\n");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            System.err.println(" ERROR generating PDF:");
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
            System.err.println(" ════════════════════════════════════════\n");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
