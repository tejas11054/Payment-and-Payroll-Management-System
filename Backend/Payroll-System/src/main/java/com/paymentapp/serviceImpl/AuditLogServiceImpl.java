	package com.paymentapp.serviceImpl;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.paymentapp.entity.AuditLog;
import com.paymentapp.repository.AuditLogRepository;
import com.paymentapp.service.AuditLogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

	private final AuditLogRepository auditLogRepository;

	@Override
	public void log(String action, String resourceType, Long resourceId, Long userId, String userEmail, String role) {
		AuditLog log = AuditLog.builder().actionPerformed(action).targetResourceType(resourceType).targetResourceId(resourceId)
				.performedByUserId(userId).performedByEmail(userEmail).performedByRole(role).actionTimestamp(Instant.now()).build();

		auditLogRepository.save(log);
	}
}

