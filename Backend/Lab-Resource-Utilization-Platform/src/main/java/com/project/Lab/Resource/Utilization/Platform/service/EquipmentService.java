package com.project.Lab.Resource.Utilization.Platform.service;

import com.project.Lab.Resource.Utilization.Platform.dto.AuditLogRequestDTO;
import com.project.Lab.Resource.Utilization.Platform.entity.Equipment;
import com.project.Lab.Resource.Utilization.Platform.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EquipmentService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private AuditLogService auditLogService;

    // =========================================================
    // CREATE EQUIPMENT
    // =========================================================

    public Equipment saveEquipment(Equipment equipment) {

        validateHourlyRate(equipment.getHourlyRate());

        equipment.setCreatedAt(LocalDateTime.now());

        Equipment saved = equipmentRepository.save(equipment);

        AuditLogRequestDTO audit = new AuditLogRequestDTO();

        audit.setUserId(0);
        audit.setAction("CREATE");
        audit.setModule("EQUIPMENT");
        audit.setDescription(
                "Equipment Added : "
                        + saved.getEquipmentName());

        audit.setIpAddress("SYSTEM");

        auditLogService.saveAuditLog(audit);

        return saved;
    }

    // =========================================================
    // GET ALL EQUIPMENT
    // =========================================================

    public List<Equipment> getAllEquipment() {

        return equipmentRepository.findAll();

    }

    // =========================================================
    // GET EQUIPMENT BY ID
    // =========================================================

    public Equipment getEquipmentById(Integer id) {

        return equipmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Equipment not found"));

    }

    // =========================================================
    // SEARCH EQUIPMENT
    // =========================================================

    public List<Equipment> searchEquipment(
            String equipmentName
    ) {

        return equipmentRepository
                .findByEquipmentNameContainingIgnoreCase(
                        equipmentName
                );

    }

    // =========================================================
    // FILTER BY STATUS
    // =========================================================

    public List<Equipment> getEquipmentByStatus(
            String status
    ) {

        return equipmentRepository.findByStatus(status);

    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    public Map<String, Long> getEquipmentDashboard() {

        Map<String, Long> dashboard =
                new HashMap<>();

        dashboard.put(
                "totalEquipment",
                equipmentRepository.count());

        dashboard.put(
                "availableEquipment",
                equipmentRepository.countByStatus("AVAILABLE"));

        dashboard.put(
                "bookedEquipment",
                equipmentRepository.countByStatus("BOOKED"));

        dashboard.put(
                "underMaintenanceEquipment",
                equipmentRepository.countByStatus("UNDER_MAINTENANCE"));

        return dashboard;

    }

    // =========================================================
    // UPDATE EQUIPMENT
    // =========================================================

    public Equipment updateEquipment(
            Integer id,
            Equipment updatedEquipment
    ) {

        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Equipment not found"));

        // -----------------------------------------------------
        // Validate hourly rate
        // -----------------------------------------------------

        validateHourlyRate(updatedEquipment.getHourlyRate());

        // -----------------------------------------------------
        // Update equipment fields
        // -----------------------------------------------------

        equipment.setDepartmentId(
                updatedEquipment.getDepartmentId());

        equipment.setCategoryId(
                updatedEquipment.getCategoryId());

        equipment.setEquipmentName(
                updatedEquipment.getEquipmentName());

        equipment.setModelNo(
                updatedEquipment.getModelNo());

        equipment.setSerialNo(
                updatedEquipment.getSerialNo());

        equipment.setDescription(
                updatedEquipment.getDescription());

        equipment.setPurchaseDate(
                updatedEquipment.getPurchaseDate());

        equipment.setStatus(
                updatedEquipment.getStatus());

        // IMPORTANT:
        // Update hourly rental rate
        equipment.setHourlyRate(
                updatedEquipment.getHourlyRate());

        Equipment updated =
                equipmentRepository.save(equipment);

        // -----------------------------------------------------
        // Audit log
        // -----------------------------------------------------

        AuditLogRequestDTO audit =
                new AuditLogRequestDTO();

        audit.setUserId(0);

        audit.setAction("UPDATE");

        audit.setModule("EQUIPMENT");

        audit.setDescription(
                "Equipment Updated : "
                        + updated.getEquipmentName()
                        + " | Hourly Rate : ₹"
                        + updated.getHourlyRate());

        audit.setIpAddress("SYSTEM");

        auditLogService.saveAuditLog(audit);

        return updated;

    }

    // =========================================================
    // HOURLY RATE VALIDATION
    // =========================================================

    private void validateHourlyRate(
            BigDecimal hourlyRate
    ) {

        if (hourlyRate == null) {
            throw new IllegalArgumentException(
                    "Hourly rate is required");
        }

        if (hourlyRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Hourly rate cannot be negative");
        }

    }

    // =========================================================
    // DELETE EQUIPMENT
    // =========================================================

    public void deleteEquipment(
            Integer id
    ) {

        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Equipment not found"));

        AuditLogRequestDTO audit =
                new AuditLogRequestDTO();

        audit.setUserId(0);

        audit.setAction("DELETE");

        audit.setModule("EQUIPMENT");

        audit.setDescription(
                "Equipment Deleted : "
                        + equipment.getEquipmentName());

        audit.setIpAddress("SYSTEM");

        auditLogService.saveAuditLog(audit);

        equipmentRepository.delete(equipment);

    }

}