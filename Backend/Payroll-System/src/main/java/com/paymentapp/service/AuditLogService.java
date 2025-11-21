package com.paymentapp.service;

public interface AuditLogService {
	
	public void log(String action, String resourceType, Long resourceId, Long userId, String userEmail, String role);

}
