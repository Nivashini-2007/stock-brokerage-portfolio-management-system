package com.stockbroker.backend.serviceimpl;

import com.stockbroker.backend.entity.AuditLog;
import com.stockbroker.backend.repository.AuditLogRepository;
import com.stockbroker.backend.security.SecurityUtils;
import com.stockbroker.backend.service.AuditLogService;
import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void record(String action, String entityType, String entityId, String details) {

        AuditLog auditLog = new AuditLog();

        String username;
        try {
            username = SecurityUtils.currentPrincipal().getUsername();
        } catch (Exception e) {
            username = "SYSTEM";
        }

        auditLog.setUsername(username);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(details);

        auditLogRepository.save(auditLog);
    }
}
