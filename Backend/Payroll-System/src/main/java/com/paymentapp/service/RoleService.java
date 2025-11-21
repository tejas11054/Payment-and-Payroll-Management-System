package com.paymentapp.service;

import com.paymentapp.dto.RoleRequestDTO;
import com.paymentapp.dto.RoleResponseDTO;

import java.util.List;

public interface RoleService {
	
	RoleResponseDTO createRole(RoleRequestDTO dto);

	RoleResponseDTO getRoleById(Long roleId);

	List<RoleResponseDTO> getAllRoles();

	RoleResponseDTO updateRole(Long roleId, RoleRequestDTO dto);

	void deleteRole(Long roleId);
}
