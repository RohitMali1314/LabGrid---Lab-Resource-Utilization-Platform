package com.project.Lab.Resource.Utilization.Platform.dto;

import java.math.BigDecimal;

public class EquipmentUtilizationDTO {

    private Integer equipmentId;
    private String equipmentName;
    private String modelNo;
    private String status;

    private Long usageSessions;
    private Long usageMinutes;

    private BigDecimal usageHours;
    private BigDecimal availableHours;
    private BigDecimal idleHours;
    private BigDecimal utilizationPercentage;

    public EquipmentUtilizationDTO() {
    }

    public Integer getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Integer equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getModelNo() {
        return modelNo;
    }

    public void setModelNo(String modelNo) {
        this.modelNo = modelNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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