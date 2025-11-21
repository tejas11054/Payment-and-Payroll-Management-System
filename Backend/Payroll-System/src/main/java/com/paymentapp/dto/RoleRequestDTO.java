package com.paymentapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RoleRequestDTO {

	@NotBlank(message = "Role name is required")
	@Pattern(regexp = "^ROLE_.*", message = "Role name must start with 'ROLE_'")
	private String roleName;
}
