package com.project.Lab.Resource.Utilization.Platform.controller;

import com.project.Lab.Resource.Utilization.Platform.dto.ReportResponseDTO;
import com.project.Lab.Resource.Utilization.Platform.entity.Booking;
import com.project.Lab.Resource.Utilization.Platform.entity.Equipment;
import com.project.Lab.Resource.Utilization.Platform.entity.Maintenance;
import com.project.Lab.Resource.Utilization.Platform.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.project.Lab.Resource.Utilization.Platform.service.ExcelReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.project.Lab.Resource.Utilization.Platform.service.PdfReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.List;
import com.project.Lab.Resource.Utilization.Platform.dto.CostAnalysisDTO;
@RestController
@RequestMapping("/api/reports")
@CrossOrigin("*")
public class ReportController {

    @Autowired
    private ReportService reportService;
    @Autowired
    private ExcelReportService excelReportService;
    @Autowired
    private PdfReportService pdfReportService;

    // =========================================================
    // DASHBOARD REPORT
    // =========================================================

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ReportResponseDTO getDashboardReport() {

        return reportService.getDashboardReport();

    }
    // =========================================================
// COST ANALYSIS REPORT
// =========================================================

    @GetMapping("/cost-analysis")
    @PreAuthorize("isAuthenticated()")
    public List<CostAnalysisDTO> getCostAnalysis() {

        return reportService.getCostAnalysis();
    }

    // =========================================================
    // EQUIPMENT REPORT
    // =========================================================

    @GetMapping("/equipment")
    @PreAuthorize("isAuthenticated()")
    public List<Equipment> getEquipmentReport() {

        return reportService.getEquipmentReport();

    }

    // =========================================================
    // BOOKING REPORT
    // =========================================================

    @GetMapping("/bookings")
    @PreAuthorize("isAuthenticated()")
    public List<Booking> getBookingReport() {

        return reportService.getBookingReport();

    }

    // =========================================================
    // MAINTENANCE REPORT
    // =========================================================

    @GetMapping("/maintenance")
    @PreAuthorize("isAuthenticated()")
    public List<Maintenance> getMaintenanceReport() {

        return reportService.getMaintenanceReport();

    }

    // =========================================================
    // TODAY BOOKINGS
    // =========================================================

    @GetMapping("/today-bookings")
    @PreAuthorize("isAuthenticated()")
    public Long getTodayBookings() {

        return reportService.getTodayBookings();

    }

    // =========================================================
    // CURRENT MONTH BOOKINGS
    // =========================================================

    @GetMapping("/month-bookings")
    @PreAuthorize("isAuthenticated()")
    public Long getCurrentMonthBookings() {

        return reportService.getCurrentMonthBookings();

    }

    // =========================================================
    // WEEKLY UTILIZATION
    // =========================================================

    @GetMapping("/weekly-utilization")
    @PreAuthorize("isAuthenticated()")
    public List<Object[]> getWeeklyUtilization() {

        return reportService.getWeeklyUtilization();

    }

    // =========================================================
    // EQUIPMENT USAGE
    // =========================================================

    @GetMapping("/equipment-usage")
    @PreAuthorize("isAuthenticated()")
    public List<Object[]> getEquipmentUsage() {

        return reportService.getEquipmentUsageReport();

    }
    // =========================================================
// EXPORT EQUIPMENT EXCEL
// =========================================================

    @GetMapping("/export/equipment/excel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> exportEquipmentExcel() throws Exception {

        return ResponseEntity.ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=equipment_report.xlsx")

                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))

                .body(
                        excelReportService.exportEquipmentExcel());
    }
    // =========================================================
// EXPORT BOOKING EXCEL
// =========================================================

    @GetMapping("/export/booking/excel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> exportBookingExcel() throws Exception {

        return ResponseEntity.ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=booking_report.xlsx")

                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))

                .body(
                        excelReportService.exportBookingExcel());
    }// =========================================================
// EXPORT MAINTENANCE EXCEL
// =========================================================

    @GetMapping("/export/maintenance/excel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> exportMaintenanceExcel() throws Exception {

        return ResponseEntity.ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=maintenance_report.xlsx")

                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))

                .body(
                        excelReportService.exportMaintenanceExcel());
    }
    // =========================================================
// EXPORT EQUIPMENT PDF
// =========================================================

    @GetMapping("/export/equipment/pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> exportEquipmentPdf() throws Exception {

        return ResponseEntity.ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=equipment_report.pdf")

                .contentType(MediaType.APPLICATION_PDF)

                .body(
                        pdfReportService.exportEquipmentPdf());
    }
    // =========================================================
// EXPORT BOOKING PDF
// =========================================================

    @GetMapping("/export/booking/pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> exportBookingPdf() throws Exception {

        return ResponseEntity.ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=booking_report.pdf")

                .contentType(MediaType.APPLICATION_PDF)

                .body(
                        pdfReportService.exportBookingPdf());
    }
    // =========================================================
// EXPORT MAINTENANCE PDF
// =========================================================

    @GetMapping("/export/maintenance/pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> exportMaintenancePdf() throws Exception {

        return ResponseEntity.ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=maintenance_report.pdf")

                .contentType(MediaType.APPLICATION_PDF)

                .body(
                        pdfReportService.exportMaintenancePdf());
    }

}