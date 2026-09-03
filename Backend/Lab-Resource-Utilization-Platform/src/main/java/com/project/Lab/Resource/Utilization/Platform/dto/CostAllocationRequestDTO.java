package com.project.Lab.Resource.Utilization.Platform.dto;

import java.math.BigDecimal;

public class CostAllocationRequestDTO {

    private Integer billId;
    private Integer bookingId;
    private Integer resourceShareId;

    private Integer institutionId;

    private BigDecimal allocationPercentage;

    private String remarks;

    public CostAllocationRequestDTO() {
    }

    public Integer getBillId() {
        return billId;
    }

    public void setBillId(Integer billId) {
        this.billId = billId;
    }

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public Integer getResourceShareId() {
        return resourceShareId;
    }

    public void setResourceShareId(Integer resourceShareId) {
        this.resourceShareId = resourceShareId;
    }

    public Integer getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Integer institutionId) {
        this.institutionId = institutionId;
    }

    public BigDecimal getAllocationPercentage() {
        return allocationPercentage;
    }

    public void setAllocationPercentage(BigDecimal allocationPercentage) {
        this.allocationPercentage = allocationPercentage;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}