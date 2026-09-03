package com.project.Lab.Resource.Utilization.Platform.dto;

public class DepartmentDemandDTO {

    private Integer departmentId;
    private String departmentName;

    private Long totalBookings;
    private Double demandPercentage;
    private String demandLevel;

    public DepartmentDemandDTO() {
    }

    public DepartmentDemandDTO(
            Integer departmentId,
            String departmentName,
            Long totalBookings,
            Double demandPercentage,
            String demandLevel
    ) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.totalBookings = totalBookings;
        this.demandPercentage = demandPercentage;
        this.demandLevel = demandLevel;
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

    public Long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(Long totalBookings) {
        this.totalBookings = totalBookings;
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