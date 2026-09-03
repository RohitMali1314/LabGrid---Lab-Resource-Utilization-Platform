package com.project.Lab.Resource.Utilization.Platform.dto;

public class ReportResponseDTO {

    private Long totalEquipment;
    private Long availableEquipment;
    private Long bookedEquipment;
    private Long underMaintenanceEquipment;

    private Long totalBookings;
    private Long pendingBookings;
    private Long approvedBookings;
    private Long completedBookings;

    private Long totalMaintenance;
    private Long pendingMaintenance;
    private Long inProgressMaintenance;
    private Long completedMaintenance;

    public ReportResponseDTO() {
    }

    public ReportResponseDTO(
            Long totalEquipment,
            Long availableEquipment,
            Long bookedEquipment,
            Long underMaintenanceEquipment,
            Long totalBookings,
            Long pendingBookings,
            Long approvedBookings,
            Long completedBookings,
            Long totalMaintenance,
            Long pendingMaintenance,
            Long inProgressMaintenance,
            Long completedMaintenance) {

        this.totalEquipment = totalEquipment;
        this.availableEquipment = availableEquipment;
        this.bookedEquipment = bookedEquipment;
        this.underMaintenanceEquipment = underMaintenanceEquipment;
        this.totalBookings = totalBookings;
        this.pendingBookings = pendingBookings;
        this.approvedBookings = approvedBookings;
        this.completedBookings = completedBookings;
        this.totalMaintenance = totalMaintenance;
        this.pendingMaintenance = pendingMaintenance;
        this.inProgressMaintenance = inProgressMaintenance;
        this.completedMaintenance = completedMaintenance;
    }

    public Long getTotalEquipment() {
        return totalEquipment;
    }

    public void setTotalEquipment(Long totalEquipment) {
        this.totalEquipment = totalEquipment;
    }

    public Long getAvailableEquipment() {
        return availableEquipment;
    }

    public void setAvailableEquipment(Long availableEquipment) {
        this.availableEquipment = availableEquipment;
    }

    public Long getBookedEquipment() {
        return bookedEquipment;
    }

    public void setBookedEquipment(Long bookedEquipment) {
        this.bookedEquipment = bookedEquipment;
    }

    public Long getUnderMaintenanceEquipment() {
        return underMaintenanceEquipment;
    }

    public void setUnderMaintenanceEquipment(Long underMaintenanceEquipment) {
        this.underMaintenanceEquipment = underMaintenanceEquipment;
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

    public Long getTotalMaintenance() {
        return totalMaintenance;
    }

    public void setTotalMaintenance(Long totalMaintenance) {
        this.totalMaintenance = totalMaintenance;
    }

    public Long getPendingMaintenance() {
        return pendingMaintenance;
    }

    public void setPendingMaintenance(Long pendingMaintenance) {
        this.pendingMaintenance = pendingMaintenance;
    }

    public Long getInProgressMaintenance() {
        return inProgressMaintenance;
    }

    public void setInProgressMaintenance(Long inProgressMaintenance) {
        this.inProgressMaintenance = inProgressMaintenance;
    }

    public Long getCompletedMaintenance() {
        return completedMaintenance;
    }

    public void setCompletedMaintenance(Long completedMaintenance) {
        this.completedMaintenance = completedMaintenance;
    }
}