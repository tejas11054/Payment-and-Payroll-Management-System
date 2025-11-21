package com.paymentapp.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.paymentapp.dto.OrgAdminRequestDTO;
import com.paymentapp.dto.OrgAdminResponseDTO;
import com.paymentapp.entity.User;

public interface OrgAdminService {

	public OrgAdminResponseDTO createOrgAdmin(Long orgId, OrgAdminRequestDTO dto, MultipartFile documentFile,
			User performingUser);

	public List<OrgAdminResponseDTO> createMultipleOrgAdmins(Long orgId, String departmentName, Long salaryGradeId,
            InputStream fileInputStream, String fileName, String fileUrl, MultipartFile documentFile,
            User performingUser) throws IOException;

	 public List<OrgAdminResponseDTO> getOrgAdminsByOrganization(Long orgId);
	
	OrgAdminResponseDTO getOrgAdminById(Long orgAdminId);
	
	List<OrgAdminResponseDTO> getOrgAdminsByDepartment(Long orgId, String departmentName);
	
	OrgAdminResponseDTO updateOrgAdmin(Long orgAdminId, OrgAdminRequestDTO dto, User performingUser);

	void deleteOrgAdmin(Long orgAdminId, User performingUser);
}
