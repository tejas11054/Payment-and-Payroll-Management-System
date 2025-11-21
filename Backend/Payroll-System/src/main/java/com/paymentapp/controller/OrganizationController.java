package com.paymentapp.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

import com.paymentapp.dto.ChangePasswordDTO;
import com.paymentapp.dto.OrganizationRegisterDTO;
import com.paymentapp.dto.OrganizationResponseDTO;
import com.paymentapp.entity.User;
import com.paymentapp.security.CustomUserDetails;
import com.paymentapp.service.OrganizationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

	private final OrganizationService organizationService;

	@PostMapping("/register")
	public ResponseEntity<OrganizationResponseDTO> registerOrganization(@RequestPart("dto") OrganizationRegisterDTO dto,
			@RequestPart("verificationDocs") MultipartFile[] verificationDocs,
			@RequestParam(value = "reactivate", required = false, defaultValue = "false") boolean reactivate) {

		OrganizationResponseDTO response = organizationService.registerOrganization(dto, verificationDocs, reactivate);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{orgId}/change-password")
	public ResponseEntity<String> changePassword(@PathVariable Long orgId,
			@RequestBody ChangePasswordDTO changePasswordDTO, @AuthenticationPrincipal CustomUserDetails userDetails) {

		User performingUser = userDetails.getUser();
		organizationService.changePassword(orgId, changePasswordDTO.getCurrentPassword(),
				changePasswordDTO.getNewPassword(), performingUser);
		return ResponseEntity.ok("Password changed successfully.");
	}

	@PostMapping("/{orgId}/request-deletion")
	public ResponseEntity<String> requestDeletion(@Valid @PathVariable Long orgId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		User performingUser = userDetails.getUser();
		organizationService.requestdeleteOrganization(orgId, performingUser);
		return ResponseEntity.ok("Deletion request submitted successfully.");
	}

	@GetMapping
	public ResponseEntity<List<OrganizationResponseDTO>> getAllOrganizations() {
		List<OrganizationResponseDTO> orgs = organizationService.getAllOrganizations();
		return ResponseEntity.ok(orgs);
	}

	@GetMapping("/{orgId}")
	public ResponseEntity<OrganizationResponseDTO> getOrganizationById(@PathVariable Long orgId) {
		OrganizationResponseDTO response = organizationService.getOrganizationById(orgId);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{orgId}")
	public ResponseEntity<OrganizationResponseDTO> updateOrganization(@PathVariable Long orgId,
			@RequestBody OrganizationRegisterDTO dto) {
		OrganizationResponseDTO updated = organizationService.updateOrganization(orgId, dto);
		return ResponseEntity.ok(updated);
	}
	@GetMapping("/{orgId}/account-balance")
	public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable Long orgId) {
	    BigDecimal balance = organizationService.getAccountBalancebyOrgId(orgId);
	    return ResponseEntity.ok(balance);
	}
	  @GetMapping("/{orgId}/details")
	    public ResponseEntity<OrganizationResponseDTO> getOrganizationWithDocuments(@PathVariable Long orgId) {
	        OrganizationResponseDTO response = organizationService.getOrganizationWithDocuments(orgId);
	        return ResponseEntity.ok(response);
	    }
}
