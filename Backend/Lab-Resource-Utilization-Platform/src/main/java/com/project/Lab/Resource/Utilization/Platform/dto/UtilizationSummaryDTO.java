package com.project.Lab.Resource.Utilization.Platform.dto;

import java.math.BigDecimal;

public class UtilizationSummaryDTO {

    private Long totalEquipment;
    private Long totalUsageSessions;
    private Long activeEquipment;
    private Long completedSessions;

    private BigDecimal totalUsageHours;
    private BigDecimal totalAvailableHours;
    private BigDecimal utilizationPercentage;
    private BigDecimal idleHours;

    public UtilizationSummaryDTO() {
    }

    public Long getTotalEquipment() {
        return totalEquipment;
    }

    public void setTotalEquipment(Long totalEquipment) {
        this.totalEquipment = totalEquipment;
    }

    public Long getTotalUsageSessions() {
        return totalUsageSessions;
    }

    public void setTotalUsageSessions(Long totalUsageSessions) {
        this.totalUsageSessions = totalUsageSessions;
    }

    public Long getActiveEquipment() {
        return activeEquipment;
    }

    public void setActiveEquipment(Long activeEquipment) {
        this.activeEquipment = activeEquipment;
    }

    public Long getCompletedSessions() {
        return completedSessions;
    }

    public void setCompletedSessions(Long completedSessions) {
        this.completedSessions = completedSessions;
    }

    public BigDecimal getTotalUsageHours() {
        return totalUsageHours;
    }

    public void setTotalUsageHours(BigDecimal totalUsageHours) {
        this.totalUsageHours = totalUsageHours;
    }

    public BigDecimal getTotalAvailableHours() {
        return totalAvailableHours;
    }

    public void setTotalAvailableHours(BigDecimal totalAvailableHours) {
        this.totalAvailableHours = totalAvailableHours;
    }

    public BigDecimal getUtilizationPercentage() {
        return utilizationPercentage;
    }

    public void setUtilizationPercentage(BigDecimal utilizationPercentage) {
        this.utilizationPercentage = utilizationPercentage;
    }

    public BigDecimal getIdleHours() {
        return idleHours;
    }

    public void setIdleHours(BigDecimal idleHours) {
        this.idleHours = idleHours;
    }
}