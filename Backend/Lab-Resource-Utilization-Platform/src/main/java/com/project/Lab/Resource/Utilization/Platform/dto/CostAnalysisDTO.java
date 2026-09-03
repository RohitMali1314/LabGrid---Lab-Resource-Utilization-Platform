package com.project.Lab.Resource.Utilization.Platform.dto;

import java.math.BigDecimal;

public class CostAnalysisDTO {

    private Integer institutionId;

    private Long totalAllocations;

    private BigDecimal totalAllocatedCost;

    private BigDecimal paidCost;

    private BigDecimal pendingCost;

    private BigDecimal approvedCost;

    private BigDecimal cancelledCost;


    public CostAnalysisDTO() {
    }


    public CostAnalysisDTO(
            Integer institutionId,
            Long totalAllocations,
            BigDecimal totalAllocatedCost,
            BigDecimal paidCost,
            BigDecimal pendingCost,
            BigDecimal approvedCost,
            BigDecimal cancelledCost
    ) {
        this.institutionId = institutionId;
        this.totalAllocations = totalAllocations;
        this.totalAllocatedCost = totalAllocatedCost;
        this.paidCost = paidCost;
        this.pendingCost = pendingCost;
        this.approvedCost = approvedCost;
        this.cancelledCost = cancelledCost;
    }


    public Integer getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Integer institutionId) {
        this.institutionId = institutionId;
    }


    public Long getTotalAllocations() {
        return totalAllocations;
    }

    public void setTotalAllocations(Long totalAllocations) {
        this.totalAllocations = totalAllocations;
    }


    public BigDecimal getTotalAllocatedCost() {
        return totalAllocatedCost;
    }

    public void setTotalAllocatedCost(
            BigDecimal totalAllocatedCost
    ) {
        this.totalAllocatedCost = totalAllocatedCost;
    }


    public BigDecimal getPaidCost() {
        return paidCost;
    }

    public void setPaidCost(
            BigDecimal paidCost
    ) {
        this.paidCost = paidCost;
    }


    public BigDecimal getPendingCost() {
        return pendingCost;
    }

    public void setPendingCost(
            BigDecimal pendingCost
    ) {
        this.pendingCost = pendingCost;
    }


    public BigDecimal getApprovedCost() {
        return approvedCost;
    }

    public void setApprovedCost(
            BigDecimal approvedCost
    ) {
        this.approvedCost = approvedCost;
    }


    public BigDecimal getCancelledCost() {
        return cancelledCost;
    }

    public void setCancelledCost(
            BigDecimal cancelledCost
    ) {
        this.cancelledCost = cancelledCost;
    }
}