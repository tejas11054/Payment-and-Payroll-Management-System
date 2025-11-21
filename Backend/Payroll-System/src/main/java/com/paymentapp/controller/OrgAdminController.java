package com.paymentapp.controller;

import java.io.InputStream;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.paymentapp.dto.OrgAdminRequestDTO;
import com.paymentapp.dto.OrgAdminResponseDTO;
import com.paymentapp.entity.User;
import com.paymentapp.repository.UserRepository;
import com.paymentapp.security.CustomUserDetails;
import com.paymentapp.service.OrgAdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orgadmins")
@RequiredArgsConstructor
public class OrgAdminController {

	private final OrgAdminService orgAdminService;
	private final UserRepository userRepository;

	@PostMapping(value = "/{orgId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<OrgAdminResponseDTO> createOrgAdmin(@Valid @PathVariable Long orgId,
			@RequestPart("dto") OrgAdminRequestDTO dto,
			@RequestPart(value = "documentFile", required = false) MultipartFile documentFile,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		OrgAdminResponseDTO response = orgAdminService.createOrgAdmin(orgId, dto, documentFile, userDetails.getUser());
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{orgId}/bulk-upload")
	public ResponseEntity<List<OrgAdminResponseDTO>> createMultipleOrgAdmins(@PathVariable Long orgId,
			@RequestParam("departmentName") String departmentName,
			@RequestParam("salaryGradeId") Long salaryGradeId,
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "fileUrl", required = false) String fileUrl,
			@RequestParam(value = "documentFile", required = false) MultipartFile documentFile,
			@AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {

		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("File must be provided");
		}

		InputStream is = file.getInputStream();
		String fname = file.getOriginalFilename();

		List<OrgAdminResponseDTO> created = orgAdminService.createMultipleOrgAdmins(orgId, departmentName, salaryGradeId, is, fname,
				fileUrl, documentFile, userDetails.getUser());

		return ResponseEntity.ok(created);
	}

	@GetMapping("/{orgId}")
	public ResponseEntity<List<OrgAdminResponseDTO>> listOrgAdmins(@PathVariable Long orgId) {
		return ResponseEntity.ok(orgAdminService.getOrgAdminsByOrganization(orgId));
	}

	@GetMapping("/admin/{orgAdminId}")
	public ResponseEntity<OrgAdminResponseDTO> getOne(@PathVariable Long orgAdminId) {
		return ResponseEntity.ok(orgAdminService.getOrgAdminById(orgAdminId));
	}

	@GetMapping("/org/{orgId}/department/{departmentName}")
	public ResponseEntity<List<OrgAdminResponseDTO>> getAdminsByDepartment(
	        @PathVariable Long orgId,
	        @PathVariable String departmentName) {
	    
	    List<OrgAdminResponseDTO> admins = orgAdminService.getOrgAdminsByDepartment(orgId, departmentName);
	    return ResponseEntity.ok(admins);
	}

	@PutMapping("/admin/{orgAdminId}")
	public ResponseEntity<OrgAdminResponseDTO> updateOrgAdmin(
	        @PathVariable Long orgAdminId,
	        @RequestBody OrgAdminRequestDTO dto,
	        @AuthenticationPrincipal CustomUserDetails userDetails) {

	    User performingUser = userDetails.getUser();
	    return ResponseEntity.ok(orgAdminService.updateOrgAdmin(orgAdminId, dto, performingUser));
	}

	@DeleteMapping("/admin/{orgAdminId}")
	public ResponseEntity<Void> deleteOrgAdmin(
	        @PathVariable Long orgAdminId,
	        @AuthenticationPrincipal CustomUserDetails userDetails) {

	    User performingUser = userDetails.getUser();
	    orgAdminService.deleteOrgAdmin(orgAdminId, performingUser);
	    return ResponseEntity.noContent().build();
	}
	
	    
	    @GetMapping("/org/{orgId}")
	    public ResponseEntity<List<OrgAdminResponseDTO>> getAdminsByOrg(
	            @PathVariable Long orgId,
	            @AuthenticationPrincipal CustomUserDetails userDetails) {
	        
	        List<OrgAdminResponseDTO> admins = orgAdminService.getOrgAdminsByOrganization(orgId);
	        return ResponseEntity.ok(admins);
	    }

}
