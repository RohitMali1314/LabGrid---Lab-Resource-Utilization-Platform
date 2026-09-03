package com.project.Lab.Resource.Utilization.Platform.dto;

import java.time.LocalDateTime;

public class AuditLogResponseDTO {

    private Integer auditId;
    private Integer userId;
    private String action;
    private String module;
    private String description;
    private String ipAddress;
    private LocalDateTime createdAt;

    public AuditLogResponseDTO() {
    }

    public AuditLogResponseDTO(
            Integer auditId,
            Integer userId,
            String action,
            String module,
            String description,
            String ipAddress,
            LocalDateTime createdAt) {

        this.auditId = auditId;
        this.userId = userId;
        this.action = action;
        this.module = module;
        this.description = description;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
    }

    public Integer getAuditId() {
        return auditId;
    }

    public void setAuditId(Integer auditId) {
        this.auditId = auditId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}