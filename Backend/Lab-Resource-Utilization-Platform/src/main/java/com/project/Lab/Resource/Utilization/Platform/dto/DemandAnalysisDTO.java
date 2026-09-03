package com.project.Lab.Resource.Utilization.Platform.dto;

public class DemandAnalysisDTO {

    private Integer equipmentId;
    private String equipmentName;

    private Long totalBookings;
    private Long pendingBookings;
    private Long approvedBookings;
    private Long completedBookings;

    private Double demandPercentage;
    private String demandLevel;

    public DemandAnalysisDTO() {
    }

    public DemandAnalysisDTO(
            Integer equipmentId,
            String equipmentName,
            Long totalBookings,
            Long pendingBookings,
            Long approvedBookings,
            Long completedBookings,
            Double demandPercentage,
            String demandLevel
    ) {
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.totalBookings = totalBookings;
        this.pendingBookings = pendingBookings;
        this.approvedBookings = approvedBookings;
        this.completedBookings = completedBookings;
        this.demandPercentage = demandPercentage;
        this.demandLevel = demandLevel;
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

    public Long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(Long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public Long getPendingBookings() {
        return pendingBookings;
    }

    public void setPendingBookings(Long pendingBookings) {
        this.pendingBookings = pendingBookings;
    }

    public Long getApprovedBookings() {
        return approvedBookings;
    }

    public void setApprovedBookings(Long approvedBookings) {
        this.approvedBookings = approvedBookings;
    }

    public Long getCompletedBookings() {
        return completedBookings;
    }

    public void setCompletedBookings(Long completedBookings) {
        this.completedBookings = completedBookings;
    }

    public Double getDemandPercentage() {
        return demandPercentage;
    }

    public void setDemandPercentage(Double demandPercentage) {
        this.demandPercentage = demandPercentage;
    }

    public String getDemandLevel() {
        return demandLevel;
    }

    public void setDemandLevel(String demandLevel) {
        this.demandLevel = demandLevel;
    }
}