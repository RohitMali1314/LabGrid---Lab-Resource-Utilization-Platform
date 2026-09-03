package com.project.Lab.Resource.Utilization.Platform.controller;

import com.project.Lab.Resource.Utilization.Platform.dto.DepartmentStatDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.UtilizationPointDTO;
import com.project.Lab.Resource.Utilization.Platform.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.project.Lab.Resource.Utilization.Platform.dto.MonthlyHeatmapDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.DemandAnalysisDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.DepartmentDemandDTO;
@RestController
@RequestMapping("/api/analytics")
@CrossOrigin("*")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    // =========================================================
    // WEEKLY UTILIZATION ANALYTICS
    // =========================================================

    @GetMapping("/utilization")
    @PreAuthorize("isAuthenticated()")
    public List<UtilizationPointDTO> getWeeklyUtilization() {
        return analyticsService.getWeeklyUtilization();
    }
    @GetMapping("/heatmap/monthly")
    @PreAuthorize("isAuthenticated()")
    public List<MonthlyHeatmapDTO> getMonthlyHeatmap() {

        return analyticsService.getMonthlyHeatmap();

    }
    // =========================================================
// DEMAND ANALYSIS
// =========================================================

    @GetMapping("/demand")
    @PreAuthorize("isAuthenticated()")
    public List<DemandAnalysisDTO> getDemandAnalysis() {

        return analyticsService.getDemandAnalysis();
    }
    // =========================================================
    // DEPARTMENT ANALYTICS
    // =========================================================

    @GetMapping("/departments")
    @PreAuthorize("isAuthenticated()")
    public List<DepartmentStatDTO> getDepartmentStatistics() {
        return analyticsService.getDepartmentStatistics();
    }
    // =========================================================
// DEPARTMENT DEMAND ANALYSIS
// =========================================================

    @GetMapping("/demand/departments")
    @PreAuthorize("isAuthenticated()")
    public List<DepartmentDemandDTO> getDepartmentDemandAnalysis() {

        return analyticsService.getDepartmentDemandAnalysis();
    }
}