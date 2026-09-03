package com.project.Lab.Resource.Utilization.Platform.dto;

import java.math.BigDecimal;

public class PeakUsageDTO {

    private String period;
    private Long usageSessions;
    private Long usageMinutes;
    private BigDecimal usageHours;

    public PeakUsageDTO() {
    }

    public PeakUsageDTO(
            String period,
            Long usageSessions,
            Long usageMinutes,
            BigDecimal usageHours
    ) {
        this.period = period;
        this.usageSessions = usageSessions;
        this.usageMinutes = usageMinutes;
        this.usageHours = usageHours;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
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
}