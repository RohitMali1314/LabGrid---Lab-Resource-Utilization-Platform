package com.project.Lab.Resource.Utilization.Platform.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MaintenanceResponseDTO {

    private Integer maintenanceId;

    private Integer equipmentId;

    private Integer technicianId;

    private Integer reportedBy;

    private String workOrderNumber;

    private String issue;

    private String issueDescription;

    private String maintenanceType;

    private String priority;

    private String status;

    private LocalDate scheduledDate;

    private LocalDate startedDate;

    private LocalDate estimatedCompletionDate;

    private LocalDate completedDate;

    private Integer downtimeHours;

    private Boolean calibrationRequired;

    private LocalDate calibrationDate;

    private LocalDate nextCalibrationDate;

    private String certificateNumber;

    private String remarks;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public MaintenanceResponseDTO() {
    }

    public MaintenanceResponseDTO(
            Integer maintenanceId,
            Integer equipmentId,
            Integer technicianId,
            Integer reportedBy,
            String workOrderNumber,
            String issue,
            String issueDescription,
            String maintenanceType,
            String priority,
            String status,
            LocalDate scheduledDate,
            LocalDate startedDate,
            LocalDate estimatedCompletionDate,
            LocalDate completedDate,
            Integer downtimeHours,
            Boolean calibrationRequired,
            LocalDate calibrationDate,
            LocalDate nextCalibrationDate,
            String certificateNumber,
            String remarks,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.maintenanceId = maintenanceId;
        this.equipmentId = equipmentId;
        this.technicianId = technicianId;
        this.reportedBy = reportedBy;
        this.workOrderNumber = workOrderNumber;
        this.issue = issue;
        this.issueDescription = issueDescription;
        this.maintenanceType = maintenanceType;
        this.priority = priority;
        this.status = status;
        this.scheduledDate = scheduledDate;
        this.startedDate = startedDate;
        this.estimatedCompletionDate = estimatedCompletionDate;
        this.completedDate = completedDate;
        this.downtimeHours = downtimeHours;
        this.calibrationRequired = calibrationRequired;
        this.calibrationDate = calibrationDate;
        this.nextCalibrationDate = nextCalibrationDate;
        this.certificateNumber = certificateNumber;
        this.remarks = remarks;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getMaintenanceId() {
        return maintenanceId;
    }

    public void setMaintenanceId(Integer maintenanceId) {
        this.maintenanceId = maintenanceId;
    }

    public Integer getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Integer equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Integer getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Integer technicianId) {
        this.technicianId = technicianId;
    }

    public Integer getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(Integer reportedBy) {
        this.reportedBy = reportedBy;
    }

    public String getWorkOrderNumber() {
        return workOrderNumber;
    }

    public void setWorkOrderNumber(String workOrderNumber) {
        this.workOrderNumber = workOrderNumber;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public String getMaintenanceType() {
        return maintenanceType;
    }

    public void setMaintenanceType(String maintenanceType) {
        this.maintenanceType = maintenanceType;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public LocalDate getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(LocalDate startedDate) {
        this.startedDate = startedDate;
    }

    public LocalDate getEstimatedCompletionDate() {
        return estimatedCompletionDate;
    }

    public void setEstimatedCompletionDate(LocalDate estimatedCompletionDate) {
        this.estimatedCompletionDate = estimatedCompletionDate;
    }

    public LocalDate getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDate completedDate) {
        this.completedDate = completedDate;
    }

    public Integer getDowntimeHours() {
        return downtimeHours;
    }

    public void setDowntimeHours(Integer downtimeHours) {
        this.downtimeHours = downtimeHours;
    }

    public Boolean getCalibrationRequired() {
        return calibrationRequired;
    }

    public void setCalibrationRequired(Boolean calibrationRequired) {
        this.calibrationRequired = calibrationRequired;
    }

    public LocalDate getCalibrationDate() {
        return calibrationDate;
    }

    public void setCalibrationDate(LocalDate calibrationDate) {
        this.calibrationDate = calibrationDate;
    }

    public LocalDate getNextCalibrationDate() {
        return nextCalibrationDate;
    }

    public void setNextCalibrationDate(LocalDate nextCalibrationDate) {
        this.nextCalibrationDate = nextCalibrationDate;
    }

    public String getCertificateNumber() {
        return certificateNumber;
    }

    public void setCertificateNumber(String certificateNumber) {
        this.certificateNumber = certificateNumber;
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