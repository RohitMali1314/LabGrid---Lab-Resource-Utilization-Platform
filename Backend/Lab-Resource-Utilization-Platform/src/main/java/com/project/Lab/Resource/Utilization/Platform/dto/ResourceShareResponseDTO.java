package com.project.Lab.Resource.Utilization.Platform.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ResourceShareResponseDTO {

    private Integer shareId;
    private Integer equipmentId;
    private Integer ownerInstitutionId;
    private Integer targetInstitutionId;
    private Integer requestedBy;
    private Integer approvedBy;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal sharingRate;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ResourceShareResponseDTO() {
    }

    public Integer getShareId() {
        return shareId;
    }

    public void setShareId(Integer shareId) {
        this.shareId = shareId;
    }

    public Integer getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Integer equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Integer getOwnerInstitutionId() {
        return ownerInstitutionId;
    }

    public void setOwnerInstitutionId(Integer ownerInstitutionId) {
        this.ownerInstitutionId = ownerInstitutionId;
    }

    public Integer getTargetInstitutionId() {
        return targetInstitutionId;
    }

    public void setTargetInstitutionId(Integer targetInstitutionId) {
        this.targetInstitutionId = targetInstitutionId;
    }

    public Integer getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(Integer requestedBy) {
        this.requestedBy = requestedBy;
    }

    public Integer getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Integer approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}