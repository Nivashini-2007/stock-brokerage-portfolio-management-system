package com.stockbroker.backend.service;

public interface AuditLogService {

    void record(String action, String entityType, String entityId, String details);
}
