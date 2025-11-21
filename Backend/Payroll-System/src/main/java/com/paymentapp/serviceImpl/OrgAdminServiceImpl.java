package com.paymentapp.serviceImpl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.paymentapp.dto.OrgAdminRequestDTO;
import com.paymentapp.dto.OrgAdminResponseDTO;
import com.paymentapp.dto.SalaryGradeResponseDTO;
import com.paymentapp.entity.Department;
import com.paymentapp.entity.OrgAdmin;
import com.paymentapp.entity.Organization;
import com.paymentapp.entity.Role;
import com.paymentapp.entity.SalaryGrade;
import com.paymentapp.entity.UploadBatch;
import com.paymentapp.entity.UploadBatchLine;
import com.paymentapp.entity.User;
import com.paymentapp.repository.DepartmentRepository;
import com.paymentapp.repository.OrgAdminRepository;
import com.paymentapp.repository.OrganizationRepository;
import com.paymentapp.repository.RoleRepository;
import com.paymentapp.repository.SalaryGradeRepository;
import com.paymentapp.repository.UploadBatchLineRepository;
import com.paymentapp.repository.UploadBatchRepository;
import com.paymentapp.repository.UserRepository;
import com.paymentapp.service.AuditLogService;
import com.paymentapp.service.DocumentService;
import com.paymentapp.service.NotificationService;
import com.paymentapp.service.OrgAdminService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrgAdminServiceImpl implements OrgAdminService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final SalaryGradeRepository salaryGradeRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final ModelMapper modelMapper;
    private final OrgAdminRepository orgAdminRepository;
    private final UploadBatchRepository uploadBatchRepository;
    private final UploadBatchLineRepository uploadBatchLineRepository;
    private final DocumentService documentService;
    private final DepartmentRepository departmentRepository;

    private static final String ROLE_ORG_ADMIN = "ROLE_ORG_ADMIN";
    private static final int MAX_EMAIL_LENGTH = 100;

    @Override
    @Transactional
    public OrgAdminResponseDTO createOrgAdmin(Long orgId, OrgAdminRequestDTO dto, MultipartFile documentFile,
            User performingUser) {
        
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        if (!"APPROVED".equalsIgnoreCase(organization.getStatus())) {
            throw new RuntimeException("Organization is not approved yet.");
        }

        String status = organization.getStatus();
        if (status == null || status.trim().equalsIgnoreCase("REJECTED")) {
            throw new RuntimeException("Operation denied: Organization is rejected");
        }

        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        // Generate or use provided password
        String pwd;
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            pwd = dto.getPassword().trim();
        } else {
            pwd = generatePassword(organization.getOrgName(), dto.getName());
        }

        Role role = roleRepository.findByRoleName(ROLE_ORG_ADMIN)
                .orElseThrow(() -> new RuntimeException("Role not found. Please add 'ROLE_ORG_ADMIN' to the roles table."));

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(pwd));
        user.setOrganization(organization);
        user.setRoles(Set.of(role));
        user.setStatus("ACTIVE");
        user = userRepository.save(user);

        // ✅ Find active department only
        Department department = null;
        if (dto.getDepartmentName() != null && !dto.getDepartmentName().trim().isEmpty()) {
            department = departmentRepository
                    .findByNameIgnoreCaseAndOrganizationAndDeletedFalse(dto.getDepartmentName().trim(), organization)
                    .orElseThrow(() -> new RuntimeException(
                            "Active department '" + dto.getDepartmentName() + "' not found in this organization."));
        }

        // ✅ Find salary grade with validation
        SalaryGrade salaryGrade = null;
        if (dto.getSalaryGradeId() != null && dto.getSalaryGradeId() > 0) {
            salaryGrade = salaryGradeRepository.findById(dto.getSalaryGradeId())
                    .orElseThrow(() -> new RuntimeException("Salary Grade not found with id: " + dto.getSalaryGradeId()));
            
            // Validate salary grade belongs to organization
            if (!salaryGrade.getOrganization().getOrgId().equals(orgId)) {
                throw new RuntimeException("Salary grade does not belong to this organization");
            }
        }

        // Create OrgAdmin
        OrgAdmin orgAdmin = new OrgAdmin();
        orgAdmin.setEmail(email);
        orgAdmin.setName(dto.getName());
        orgAdmin.setPhone(dto.getPhone());
        orgAdmin.setStatus("ACTIVE");
        orgAdmin.setOrganization(organization);
        orgAdmin.setUser(user);
        orgAdmin.setSalaryGrade(salaryGrade);
        orgAdmin.setBankAccountNo(dto.getBankAccountNo());
        orgAdmin.setIfscCode(dto.getIfscCode());
        orgAdmin.setDepartment(department);
        orgAdmin.setBankAccountName(dto.getBankAccountName());

        
        user.setOrgAdmin(orgAdmin);
        orgAdmin = orgAdminRepository.save(orgAdmin);

        // Upload document
        if (documentFile != null && !documentFile.isEmpty()) {
            try {
                documentService.uploadAndSaveDocument(documentFile, "ORG_ADMIN", orgAdmin.getOrgAdminId(),
                        "PROFILE_DOCUMENT", performingUser.getUserId(), organization);
            } catch (Exception e) {
                System.err.println("Document upload failed for orgAdminId " + orgAdmin.getOrgAdminId() + ": " + e.getMessage());
                throw new RuntimeException("Failed to upload document: " + e.getMessage(), e);
            }
        }

        // Send welcome email
        notificationService.sendEmail(email, "Welcome as Organization Admin",
                "Dear " + dto.getName() + ",\n\n" + 
                "Your account as an Organization Admin for '" + organization.getOrgName() + "' has been created.\n\n" + 
                "Login Email: " + email + "\n" + 
                "Password: " + pwd + "\n\n" + 
                "Best regards,\nPaymentApp Team");

        // Audit log
        auditLogService.log("CREATE_ORG_ADMIN", "ORG_ADMIN", user.getUserId(), performingUser.getUserId(),
                performingUser.getEmail(),
                performingUser.getRoles().stream().findFirst().map(Role::getRoleName).orElse("UNKNOWN"));

        // Build response
        OrgAdminResponseDTO responseDTO = modelMapper.map(orgAdmin, OrgAdminResponseDTO.class);
        responseDTO.setDepartmentName(department != null ? department.getName() : null);
        responseDTO.setSalaryGradeId(salaryGrade != null ? salaryGrade.getGradeId() : null);
        responseDTO.setGradeCode(salaryGrade != null ? salaryGrade.getGradeCode() : null);
        
        return responseDTO;
    }

    @Override
    @Transactional
    public List<OrgAdminResponseDTO> createMultipleOrgAdmins(Long orgId, String departmentName, Long salaryGradeId,
            InputStream fileInputStream, String fileName, String fileUrl, MultipartFile documentFile,
            User performingUser) throws IOException {

        if (fileName == null || fileName.isBlank()) {
            throw new RuntimeException("File name must be provided");
        }

        BufferedInputStream bis = new BufferedInputStream(fileInputStream);

        List<OrgAdminRequestDTO> dtos;
        UserDataImportService importService = new UserDataImportService();

        if (fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls")) {
            dtos = importService.parseOrgAdminExcel(bis);
        } else if (fileName.toLowerCase().endsWith(".csv")) {
            dtos = importService.parseOrgAdminCsv(bis);
        } else {
            throw new RuntimeException("Unsupported file type: " + fileName);
        }

        if (dtos.isEmpty()) {
            throw new RuntimeException("No valid records found in the uploaded file");
        }

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        String status = organization.getStatus();
        if (status == null || status.trim().equalsIgnoreCase("REJECTED")) {
            throw new RuntimeException("Operation denied: Organization is rejected");
        }

        // ✅ Find active department only
        Department department = null;
        if (departmentName != null && !departmentName.isBlank()) {
            department = departmentRepository
                    .findByNameIgnoreCaseAndOrganizationAndDeletedFalse(departmentName.trim(), organization)
                    .orElseThrow(() -> new RuntimeException("Active department not found: " + departmentName));
        } else {
            throw new RuntimeException("Department name must be provided");
        }

        // ✅ Find salary grade with validation
        SalaryGrade salaryGrade = null;
        if (salaryGradeId != null && salaryGradeId > 0) {
            salaryGrade = salaryGradeRepository.findById(salaryGradeId)
                    .orElseThrow(() -> new RuntimeException("Salary Grade not found with id: " + salaryGradeId));
            
            if (!salaryGrade.getOrganization().getOrgId().equals(orgId)) {
                throw new RuntimeException("Salary grade does not belong to this organization");
            }
        }

        // Create upload batch
        UploadBatch batch = new UploadBatch();
        batch.setUploadedBy(performingUser.getUserId());
        batch.setFileUrl(fileUrl != null && !fileUrl.isBlank() ? fileUrl : "Uploaded Directly");
        batch.setOrganization(organization);
        batch.setProcessedCount(0);
        batch.setStatus("PENDING");
        batch.setRecordCount(dtos.size());
        batch.setEntityType("ORG_ADMIN");

        int rowNo = 1;
        for (OrgAdminRequestDTO dto : dtos) {
            String rawData = dto.getEmail() != null ? dto.getEmail() : "";
            UploadBatchLine line = UploadBatchLine.builder()
                    .rowNumber(rowNo++)
                    .rawData(rawData)
                    .status("PENDING")
                    .message(null)
                    .entityType("ORG_ADMIN")
                    .uploadBatch(batch)
                    .build();
            batch.addBatchLine(line);
        }
        batch = uploadBatchRepository.save(batch);

        List<OrgAdminResponseDTO> createdList = new ArrayList<>();
        int processedCount = 0;

        for (int i = 0; i < dtos.size(); i++) {
            OrgAdminRequestDTO dto = dtos.get(i);
            UploadBatchLine line = batch.getLines().get(i);

            if (dto.getEmail() == null || dto.getEmail().isBlank()) {
                line.setStatus("FAILED");
                line.setMessage("Email is missing");
                uploadBatchLineRepository.save(line);
                continue;
            }

            if (dto.getEmail().length() > MAX_EMAIL_LENGTH) {
                line.setStatus("FAILED");
                line.setMessage("Email too long");
                uploadBatchLineRepository.save(line);
                continue;
            }

            String normalizedEmail = dto.getEmail().trim().toLowerCase();
            if (userRepository.existsByEmail(normalizedEmail)) {
                line.setStatus("FAILED");
                line.setMessage("Duplicate email");
                uploadBatchLineRepository.save(line);
                continue;
            }

            try {
                String pwd = generatePassword(organization.getOrgName(), dto.getName());
                Role role = roleRepository.findByRoleName(ROLE_ORG_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Role not found"));

                User user = new User();
                user.setEmail(normalizedEmail);
                user.setPassword(passwordEncoder.encode(pwd));
                user.setOrganization(organization);
                user.setRoles(Set.of(role));
                user.setStatus("ACTIVE");
                user = userRepository.save(user);

                OrgAdmin admin = new OrgAdmin();
                admin.setEmail(normalizedEmail);
                admin.setName(dto.getName());
                admin.setPhone(dto.getPhone());
                admin.setBankAccountNo(dto.getBankAccountNo());
                admin.setIfscCode(dto.getIfscCode());
                admin.setStatus("ACTIVE");
                admin.setOrganization(organization);
                admin.setUser(user);
                admin.setDepartment(department);
                admin.setSalaryGrade(salaryGrade);
                admin.setBankAccountName(dto.getBankAccountName());


                admin = orgAdminRepository.save(admin);

                line.setEntityId(admin.getOrgAdminId());
                line.setStatus("SUCCESS");
                line.setMessage("Created successfully");
                uploadBatchLineRepository.save(line);

                // Document uploads
                if (dto.getFileUrl() != null && !dto.getFileUrl().isBlank()) {
                    try {
                        documentService.uploadAndSaveDocumentFromUrl(dto.getFileUrl(), "ORG_ADMIN",
                                admin.getOrgAdminId(), "BULK_UPLOAD_DOCUMENT", performingUser.getUserId(), organization);
                    } catch (Exception e) {
                        System.err.println("Failed URL document upload for adminId=" + admin.getOrgAdminId() + ": " + e.getMessage());
                    }
                }

                if (documentFile != null && !documentFile.isEmpty()) {
                    try {
                        documentService.uploadAndSaveDocument(documentFile, "ORG_ADMIN", admin.getOrgAdminId(),
                                "BULK_UPLOAD_DOCUMENT", performingUser.getUserId(), organization);
                    } catch (Exception e) {
                        System.err.println("Failed manual document upload for adminId=" + admin.getOrgAdminId() + ": " + e.getMessage());
                    }
                }

                OrgAdminResponseDTO responseDTO = modelMapper.map(admin, OrgAdminResponseDTO.class);
                responseDTO.setDepartmentName(department.getName());
                responseDTO.setSalaryGradeId(salaryGrade != null ? salaryGrade.getGradeId() : null);
                responseDTO.setGradeCode(salaryGrade != null ? salaryGrade.getGradeCode() : null);
                createdList.add(responseDTO);

                // Send email
                notificationService.sendEmail(normalizedEmail, "Welcome to PaymentApp - Org Admin Account Created",
                        String.format("Dear %s,\n\nYour organization admin account has been created successfully.\n" +
                                "Your default password is: %s\nPlease change your password after login.\n\n" +
                                "Best,\nPaymentApp Team", dto.getName(), pwd));

                // Audit log
                auditLogService.log("CREATE_ORG_ADMIN", "ORG_ADMIN", user.getUserId(), performingUser.getUserId(),
                        performingUser.getEmail(),
                        performingUser.getRoles().stream().map(Role::getRoleName).findFirst().orElse("UNKNOWN"));

                processedCount++;

            } catch (Exception ex) {
                ex.printStackTrace();
                line.setStatus("FAILED");
                line.setMessage("Error: " + ex.getMessage());
                uploadBatchLineRepository.save(line);
            }
        }

        batch.setProcessedCount(processedCount);
        batch.setStatus(processedCount == dtos.size() ? "COMPLETED" : "PARTIALLY_COMPLETED");
        uploadBatchRepository.save(batch);

        // Bulk audit log
        auditLogService.log("BULK_CREATE_ORG_ADMIN", "ORG_ADMIN", performingUser.getUserId(),
                performingUser.getUserId(), performingUser.getEmail(),
                performingUser.getRoles().stream().findFirst().map(Role::getRoleName).orElse("UNKNOWN"));

        return createdList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrgAdminResponseDTO> getOrgAdminsByOrganization(Long orgId) {
        System.out.println("🔍 Fetching org admins for orgId: " + orgId);
        
        List<OrgAdmin> admins = orgAdminRepository.findByOrganization_OrgId(orgId);
        
        System.out.println("✅ Found " + admins.size() + " org admins");
        
        return admins.stream()
                .filter(admin -> !admin.isDeleted())
                .map(admin -> {
                    OrgAdminResponseDTO dto = new OrgAdminResponseDTO();
                    dto.setOrgAdminId(admin.getOrgAdminId());
                    dto.setName(admin.getName());
                    dto.setEmail(admin.getEmail());
                    dto.setPhone(admin.getPhone());
                    dto.setOrganizationId(orgId);
                    
                    // ✅ Include salary grade
                    if (admin.getSalaryGrade() != null) {
                        SalaryGrade grade = admin.getSalaryGrade();
                        
                        dto.setSalaryGradeId(grade.getGradeId());
                        dto.setGradeCode(grade.getGradeCode());
                        
                        SalaryGradeResponseDTO gradeDTO = SalaryGradeResponseDTO.builder()
                                .gradeId(grade.getGradeId())
                                .gradeCode(grade.getGradeCode())
                                .basicSalary(grade.getBasicSalary())
                                .hra(grade.getHra())
                                .da(grade.getDa())
                                .allowances(grade.getAllowances())
                                .pf(grade.getPf())
                                .organizationId(grade.getOrganization() != null ? 
                                              grade.getOrganization().getOrgId() : null)
                                .build();
                        
                        dto.setSalaryGrade(gradeDTO);
                        
                        System.out.println("✅ Admin " + admin.getName() + " - Grade: " + grade.getGradeCode());
                    } else {
                        System.out.println("⚠️ Admin " + admin.getName() + " has NO salary grade!");
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public OrgAdminResponseDTO getOrgAdminById(Long orgAdminId) {
        OrgAdmin orgAdmin = orgAdminRepository.findById(orgAdminId)
                .orElseThrow(() -> new RuntimeException("Org Admin not found"));

        User user = orgAdmin.getUser();
        if (user == null) {
            throw new RuntimeException("Associated user not found");
        }

        boolean isOrgAdmin = user.getRoles().stream()
                .anyMatch(r -> ROLE_ORG_ADMIN.equals(r.getRoleName()));
        if (!isOrgAdmin) {
            throw new RuntimeException("User is not an Org Admin");
        }

        OrgAdminResponseDTO dto = modelMapper.map(orgAdmin, OrgAdminResponseDTO.class);
        dto.setDepartmentName(orgAdmin.getDepartment() != null ? orgAdmin.getDepartment().getName() : null);
        dto.setSalaryGradeId(orgAdmin.getSalaryGrade() != null ? orgAdmin.getSalaryGrade().getGradeId() : null);
        dto.setGradeCode(orgAdmin.getSalaryGrade() != null ? orgAdmin.getSalaryGrade().getGradeCode() : null);
        return dto;
    }


    @Override
    public List<OrgAdminResponseDTO> getOrgAdminsByDepartment(Long orgId, String departmentName) {
        
        // ✅ Validate organization
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        // ✅ Find active department with organization filter
        Department department = departmentRepository
                .findByNameIgnoreCaseAndOrganizationAndDeletedFalse(departmentName.trim(), organization)
                .orElseThrow(() -> new RuntimeException(
                    "Active department '" + departmentName + "' not found in this organization"));

        // ✅ Get org admins in that department
        List<OrgAdmin> orgAdmins = orgAdminRepository.findByDepartment(department);

        return orgAdmins.stream()
                .filter(admin -> !admin.isDeleted()) // ✅ Only active admins
                .map(orgAdmin -> {
                    OrgAdminResponseDTO dto = modelMapper.map(orgAdmin, OrgAdminResponseDTO.class);
                    dto.setDepartmentName(department.getName());
                    dto.setSalaryGradeId(orgAdmin.getSalaryGrade() != null ? orgAdmin.getSalaryGrade().getGradeId() : null);
                    dto.setGradeCode(orgAdmin.getSalaryGrade() != null ? orgAdmin.getSalaryGrade().getGradeCode() : null);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrgAdminResponseDTO updateOrgAdmin(Long orgAdminId, OrgAdminRequestDTO dto, User performingUser) {
        
        OrgAdmin orgAdmin = orgAdminRepository.findById(orgAdminId)
                .orElseThrow(() -> new RuntimeException("Org Admin not found"));

        User targetUser = orgAdmin.getUser();

        if (targetUser == null || targetUser.isDeleted()) {
            throw new RuntimeException("Org Admin is deleted or user missing");
        }

        if (!"ACTIVE".equalsIgnoreCase(orgAdmin.getStatus())) {
            throw new RuntimeException("Org Admin is not ACTIVE");
        }

        Organization organization = orgAdmin.getOrganization();
        if (organization == null) {
            throw new RuntimeException("Organization not found for this admin");
        }

        // Authorization checks
        boolean isSelfUpdate = targetUser.getUserId().equals(performingUser.getUserId());
        boolean isOrganizationUser = performingUser.getRoles().stream()
                .anyMatch(role -> role.getRoleName().equals("ROLE_ORGANIZATION"));

        if (!isSelfUpdate && !isOrganizationUser) {
            throw new RuntimeException("Unauthorized: Only the Org Admin or Organization user can update");
        }

        // Update email
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            String newEmail = dto.getEmail().trim().toLowerCase();
            if (!targetUser.getEmail().equalsIgnoreCase(newEmail)) {
                boolean emailUsedByAnother = userRepository.existsByEmailAndUserIdNot(newEmail, targetUser.getUserId());
                if (emailUsedByAnother) {
                    throw new RuntimeException("Email already in use");
                }
                targetUser.setEmail(newEmail);
                orgAdmin.setEmail(newEmail);
            }
        }

        // Update name
        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            orgAdmin.setName(dto.getName().trim());
        }

        // Update phone
        if (dto.getPhone() != null && !dto.getPhone().trim().isEmpty()) {
            orgAdmin.setPhone(dto.getPhone().trim());
        }
        if (dto.getBankAccountName() != null && !dto.getBankAccountName().trim().isEmpty()) {
            orgAdmin.setBankAccountName(dto.getBankAccountName().trim());
        }


        // ✅ Update department (active only)
        if (dto.getDepartmentName() != null && !dto.getDepartmentName().isBlank()) {
            Department dept = departmentRepository
                    .findFirstByNameAndOrganizationAndDeletedFalse(dto.getDepartmentName().trim(), organization)
                    .orElseThrow(() -> new RuntimeException(
                            "Department '" + dto.getDepartmentName() + "' not found or is deleted"));
            
            orgAdmin.setDepartment(dept);
        } else if (dto.getDepartmentName() != null && dto.getDepartmentName().isBlank()) {
            orgAdmin.setDepartment(null);
        }

        // Update salary grade
        if (dto.getSalaryGradeId() != null && dto.getSalaryGradeId() > 0) {
            SalaryGrade newSalaryGrade = salaryGradeRepository.findById(dto.getSalaryGradeId())
                    .orElseThrow(() -> new RuntimeException("Salary Grade not found"));
            
            if (!newSalaryGrade.getOrganization().getOrgId().equals(organization.getOrgId())) {
                throw new RuntimeException("Salary grade does not belong to your organization");
            }
            
            orgAdmin.setSalaryGrade(newSalaryGrade);
        } else if (dto.getSalaryGradeId() != null && dto.getSalaryGradeId() == 0) {
            orgAdmin.setSalaryGrade(null);
        }

        // Update bank details
        if (dto.getBankAccountNo() != null && !dto.getBankAccountNo().trim().isEmpty()) {
            orgAdmin.setBankAccountNo(dto.getBankAccountNo().trim());
        }

        if (dto.getIfscCode() != null && !dto.getIfscCode().trim().isEmpty()) {
            orgAdmin.setIfscCode(dto.getIfscCode().trim());
        }

        // Save changes
        userRepository.save(targetUser);
        orgAdminRepository.save(orgAdmin);

        // Audit log
        System.out.println("Calling audit log service started...");

        auditLogService.log("UPDATE_ORG_ADMIN", "ORG_ADMIN", targetUser.getUserId(), performingUser.getUserId(),
                performingUser.getEmail(),
                String.format("OrgAdmin updated: %s by %s", targetUser.getEmail(), performingUser.getEmail()));
        System.out.println("Calling audit log service ended...");

        // Build response
        OrgAdminResponseDTO responseDTO = modelMapper.map(orgAdmin, OrgAdminResponseDTO.class);
        responseDTO.setDepartmentName(orgAdmin.getDepartment() != null ? orgAdmin.getDepartment().getName() : null);
        responseDTO.setSalaryGradeId(orgAdmin.getSalaryGrade() != null ? orgAdmin.getSalaryGrade().getGradeId() : null);
        responseDTO.setGradeCode(orgAdmin.getSalaryGrade() != null ? orgAdmin.getSalaryGrade().getGradeCode() : null);

        return responseDTO;
    }

    @Override
    @Transactional
    public void deleteOrgAdmin(Long orgAdminId, User performingUser) {
        OrgAdmin orgAdmin = orgAdminRepository.findById(orgAdminId)
                .orElseThrow(() -> new RuntimeException("Org Admin not found"));

        User user = orgAdmin.getUser();

        if (user == null) {
            throw new RuntimeException("User not found for Org Admin");
        }

        boolean isOrgAdmin = user.getRoles().stream()
                .anyMatch(role -> ROLE_ORG_ADMIN.equals(role.getRoleName()));

        if (!isOrgAdmin) {
            throw new RuntimeException("User is not an Org Admin");
        }

        // Soft delete
        user.setDeleted(true);
        user.setStatus("INACTIVE");
        userRepository.save(user);

        orgAdmin.setStatus("DELETED");
        orgAdmin.setDeleted(true);
        orgAdminRepository.save(orgAdmin);

        // Send notification
        notificationService.sendEmail(user.getEmail(), "Account Deletion Confirmation", 
                "Dear Admin,\n\n" +
                "Your organization admin account has been deleted successfully.\n\n" +
                "If you did not request this action, please contact support immediately.\n\n" +
                "Best regards,\nPaymentApp Team");

        // Audit log
        auditLogService.log("DELETE_ORG_ADMIN", "ORG_ADMIN", user.getUserId(), performingUser.getUserId(),
                performingUser.getEmail(),
                performingUser.getRoles().stream().findFirst().map(Role::getRoleName).orElse("UNKNOWN"));
    }

    /**
     * Generate random password for org admin
     */
    private String generatePassword(String orgName, String orgAdminName) {
        String orgPart = orgName.length() >= 2 ? orgName.substring(0, 2) : orgName;
        String adminPart = orgAdminName.length() >= 2 ? orgAdminName.substring(0, 2) : orgAdminName;

        StringBuilder sb = new StringBuilder();
        sb.append(orgPart);
        sb.append(adminPart);
        sb.append("@");

        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 4; i++) {
            int digit = random.nextInt(10);
            sb.append(digit);
        }
        return sb.toString();
    }
    
}
