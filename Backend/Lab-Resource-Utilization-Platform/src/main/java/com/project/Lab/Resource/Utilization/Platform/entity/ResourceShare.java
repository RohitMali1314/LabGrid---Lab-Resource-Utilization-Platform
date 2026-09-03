package com.project.Lab.Resource.Utilization.Platform.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "resource_shares")
public class ResourceShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "share_id")
    private Integer shareId;

    @Column(name = "equipment_id", nullable = false)
    private Integer equipmentId;

    @Column(name = "owner_institution_id", nullable = false)
    private Integer ownerInstitutionId;

    @Column(name = "target_institution_id", nullable = false)
    private Integer targetInstitutionId;

    @Column(name = "requested_by")
    private Integer requestedBy;

    @Column(name = "approved_by")
    private Integer approvedBy;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "sharing_rate", precision = 12, scale = 2)
    private BigDecimal sharingRate;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ResourceShare() {
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