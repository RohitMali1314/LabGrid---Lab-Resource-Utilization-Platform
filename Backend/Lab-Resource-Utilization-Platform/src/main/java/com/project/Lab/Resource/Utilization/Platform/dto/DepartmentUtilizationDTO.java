package com.project.Lab.Resource.Utilization.Platform.dto;

import java.math.BigDecimal;

public class DepartmentUtilizationDTO {

    private Integer departmentId;
    private String departmentName;

    private Long totalEquipment;
    private Long usageSessions;
    private Long usageMinutes;

    private BigDecimal usageHours;
    private BigDecimal availableHours;
    private BigDecimal idleHours;
    private BigDecimal utilizationPercentage;

    public DepartmentUtilizationDTO() {
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Long getTotalEquipment() {
        return totalEquipment;
    }

    public void setTotalEquipment(Long totalEquipment) {
        this.totalEquipment = totalEquipment;
    }

    public Long getUsageSessions() {
        return usageSessions;
    }

    public void setUsageSessions(Long usageSessions) {
        this.usageSessions = usageSessions;
    }

    public Long getUsageMinutes() {
        return usageMinutes;
    }

    public void setUsageMinutes(Long usageMinutes) {
        this.usageMinutes = usageMinutes;
    }

    public BigDecimal getUsageHours() {
        return usageHours;
    }

    public void setUsageHours(BigDecimal usageHours) {
        this.usageHours = usageHours;
    }

    public BigDecimal getAvailableHours() {
        return availableHours;
    }

    public void setAvailableHours(BigDecimal availableHours) {
        this.availableHours = availableHours;
    }

    public BigDecimal getIdleHours() {
        return idleHours;
    }

    public void setIdleHours(BigDecimal idleHours) {
        this.idleHours = idleHours;
    }

    public BigDecimal getUtilizationPercentage() {
        return utilizationPercentage;
    }

    public void setUtilizationPercentage(BigDecimal utilizationPercentage) {
        this.utilizationPercentage = utilizationPercentage;
    }
}