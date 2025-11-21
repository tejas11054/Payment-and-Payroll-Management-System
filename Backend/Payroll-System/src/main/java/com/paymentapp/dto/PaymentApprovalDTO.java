package com.paymentapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentApprovalDTO {
	private Long paymentId;
	private String action;
	private String comment;
}
