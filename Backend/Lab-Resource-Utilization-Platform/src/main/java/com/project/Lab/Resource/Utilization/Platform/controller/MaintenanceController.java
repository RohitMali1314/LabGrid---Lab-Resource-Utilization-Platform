package com.project.Lab.Resource.Utilization.Platform.controller;

import com.project.Lab.Resource.Utilization.Platform.dto.MaintenanceRequestDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.MaintenanceResponseDTO;
import com.project.Lab.Resource.Utilization.Platform.service.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
@CrossOrigin("*")
public class MaintenanceController {

    @Autowired
    private MaintenanceService maintenanceService;

    // ==========================================================
    // CREATE MAINTENANCE
    // ==========================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('LAB_MANAGER','LAB_TECHNICIAN')")
    public MaintenanceResponseDTO create(@RequestBody MaintenanceRequestDTO dto) {

        return maintenanceService.create(dto);
    }

    // ==========================================================
    // GET ALL
    // ==========================================================

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<MaintenanceResponseDTO> getAll() {

        return maintenanceService.getAll();
    }

    // ==========================================================
    // GET BY ID
    // ==========================================================

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public MaintenanceResponseDTO getById(@PathVariable Integer id) {

        return maintenanceService.getById(id);
    }

    // ==========================================================
    // GET BY EQUIPMENT
    // ==========================================================

    @GetMapping("/equipment/{equipmentId}")
    @PreAuthorize("isAuthenticated()")
    public List<MaintenanceResponseDTO> getByEquipment(
            @PathVariable Integer equipmentId) {

        return maintenanceService.getByEquipment(equipmentId);
    }

    // ==========================================================
    // GET BY TECHNICIAN
    // ==========================================================

    @GetMapping("/technician/{technicianId}")
    @PreAuthorize("isAuthenticated()")
    public List<MaintenanceResponseDTO> getByTechnician(
            @PathVariable Integer technicianId) {

        return maintenanceService.getByTechnician(technicianId);
    }

    // ==========================================================
    // GET BY STATUS
    // ==========================================================

    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public List<MaintenanceResponseDTO> getByStatus(
            @PathVariable String status) {

        return maintenanceService.getByStatus(status);
    }

    // ==========================================================
    // GET BY PRIORITY
    // ==========================================================

    @GetMapping("/priority/{priority}")
    @PreAuthorize("isAuthenticated()")
    public List<MaintenanceResponseDTO> getByPriority(
            @PathVariable String priority) {

        return maintenanceService.getByPriority(priority);
    }

    // ==========================================================
    // CALIBRATION PENDING
    // ==========================================================

    @GetMapping("/calibration/pending")
    @PreAuthorize("hasAnyRole('LAB_MANAGER','LAB_TECHNICIAN')")
    public List<MaintenanceResponseDTO> calibrationPending() {

        return maintenanceService.getCalibrationPending();
    }

    // ==========================================================
    // START MAINTENANCE
    // ==========================================================

    @PutMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('LAB_MANAGER','LAB_TECHNICIAN')")
    public MaintenanceResponseDTO start(@PathVariable Integer id) {

        return maintenanceService.startMaintenance(id);
    }

    // ==========================================================
    // COMPLETE MAINTENANCE
    // ==========================================================

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('LAB_MANAGER','LAB_TECHNICIAN')")
    public MaintenanceResponseDTO complete(
            @PathVariable Integer id,
            @RequestParam String remarks) {

        return maintenanceService.complete(id, remarks);
    }

    // ==========================================================
    // DASHBOARD
    // ==========================================================

    @GetMapping("/dashboard/total")
    public long total() {
        return maintenanceService.totalMaintenance();
    }

    @GetMapping("/dashboard/pending")
    public long pending() {
        return maintenanceService.pendingMaintenance();
    }

    @GetMapping("/dashboard/in-progress")
    public long inProgress() {
        return maintenanceService.inProgressMaintenance();
    }

    @GetMapping("/dashboard/completed")
    public long completed() {
        return maintenanceService.completedMaintenance();
    }

    @GetMapping("/dashboard/preventive")
    public long preventive() {
        return maintenanceService.preventiveMaintenance();
    }

    @GetMapping("/dashboard/corrective")
    public long corrective() {
        return maintenanceService.correctiveMaintenance();
    }

    @GetMapping("/dashboard/calibration")
    public long calibration() {
        return maintenanceService.calibrationMaintenance();
    }

}