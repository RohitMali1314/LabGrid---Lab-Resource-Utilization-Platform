package com.project.Lab.Resource.Utilization.Platform.repository;

import com.project.Lab.Resource.Utilization.Platform.entity.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, Integer> {

    // Equipment
    List<Maintenance> findByEquipmentId(Integer equipmentId);

    // Technician
    List<Maintenance> findByTechnicianId(Integer technicianId);

    // Reporter
    List<Maintenance> findByReportedBy(Integer reportedBy);

    // Status
    List<Maintenance> findByStatus(String status);

    // Type
    List<Maintenance> findByMaintenanceType(String maintenanceType);

    // Priority
    List<Maintenance> findByPriority(String priority);

    // Calibration
    List<Maintenance> findByCalibrationRequired(Boolean calibrationRequired);

    List<Maintenance> findByNextCalibrationDateBefore(LocalDate date);

    // Scheduled Maintenance
    List<Maintenance> findByScheduledDate(LocalDate scheduledDate);

    List<Maintenance> findByScheduledDateBetween(LocalDate startDate, LocalDate endDate);

    // Completed
    List<Maintenance> findByCompletedDate(LocalDate completedDate);

    // Work Order
    Optional<Maintenance> findByWorkOrderNumber(String workOrderNumber);

    boolean existsByWorkOrderNumber(String workOrderNumber);

    // Equipment + Status
    List<Maintenance> findByEquipmentIdAndStatus(Integer equipmentId, String status);

    // Dashboard
    long countByStatus(String status);

    long countByMaintenanceType(String maintenanceType);

    long countByPriority(String priority);

    long countByCalibrationRequired(Boolean calibrationRequired);
    // Preventive maintenance due today
    List<Maintenance> findByScheduledDateAndStatus(
            LocalDate scheduledDate,
            String status);

    // Overdue maintenance
    List<Maintenance> findByScheduledDateBeforeAndStatus(
            LocalDate scheduledDate,
            String status);

    // Calibration due today
    List<Maintenance> findByNextCalibrationDateAndCalibrationRequired(
            LocalDate nextCalibrationDate,
            Boolean calibrationRequired);

    // Calibration overdue
    List<Maintenance> findByNextCalibrationDateBeforeAndCalibrationRequired(
            LocalDate nextCalibrationDate,
            Boolean calibrationRequired);
}