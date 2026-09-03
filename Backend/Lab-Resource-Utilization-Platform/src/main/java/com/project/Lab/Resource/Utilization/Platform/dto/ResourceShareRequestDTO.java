package com.project.Lab.Resource.Utilization.Platform.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ResourceShareRequestDTO {

    private Integer equipmentId;
    private Integer targetInstitutionId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal sharingRate;
    private String remarks;

    public ResourceShareRequestDTO() {
    }

    public Integer getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Integer equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Integer getTargetInstitutionId() {
        return targetInstitutionId;
    }

    public void setTargetInstitutionId(Integer targetInstitutionId) {
        this.targetInstitutionId = targetInstitutionId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getSharingRate() {
        return sharingRate;
    }

    public void setSharingRate(BigDecimal sharingRate) {
        this.sharingRate = sharingRate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}