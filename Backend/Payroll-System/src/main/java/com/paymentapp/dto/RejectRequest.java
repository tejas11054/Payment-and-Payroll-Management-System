package com.paymentapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RejectRequest {
	
	@NotBlank(message = "Rejection reason is required")
	private String reason;

}
