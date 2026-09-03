package com.project.Lab.Resource.Utilization.Platform.service;

import com.project.Lab.Resource.Utilization.Platform.dto.MaintenanceRequestDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.MaintenanceResponseDTO;
import com.project.Lab.Resource.Utilization.Platform.entity.Equipment;
import com.project.Lab.Resource.Utilization.Platform.entity.Maintenance;
import com.project.Lab.Resource.Utilization.Platform.repository.EquipmentRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.MaintenanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.project.Lab.Resource.Utilization.Platform.dto.AuditLogRequestDTO;
import com.project.Lab.Resource.Utilization.Platform.entity.Notification;
import com.project.Lab.Resource.Utilization.Platform.repository.NotificationRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.project.Lab.Resource.Utilization.Platform.entity.User;
import com.project.Lab.Resource.Utilization.Platform.repository.UserRepository;


@Service
public class MaintenanceService {

    @Autowired
    private MaintenanceRepository maintenanceRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;
    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private NotificationWebSocketService notificationWebSocketService;

    // =========================================================
    // CREATE MAINTENANCE
    // =========================================================

    public MaintenanceResponseDTO create(MaintenanceRequestDTO dto) {

        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        if ("UNDER_MAINTENANCE".equalsIgnoreCase(equipment.getStatus())) {
            throw new RuntimeException("Equipment is already under maintenance.");
        }

        Maintenance maintenance = new Maintenance();

        maintenance.setEquipmentId(dto.getEquipmentId());
        maintenance.setTechnicianId(dto.getTechnicianId());
        maintenance.setReportedBy(dto.getReportedBy());

        maintenance.setIssue(dto.getIssue());
        maintenance.setIssueDescription(dto.getIssueDescription());

        maintenance.setMaintenanceType(dto.getMaintenanceType());
        maintenance.setPriority(dto.getPriority());

        maintenance.setScheduledDate(dto.getScheduledDate());
        maintenance.setEstimatedCompletionDate(dto.getEstimatedCompletionDate());

        maintenance.setCalibrationRequired(dto.getCalibrationRequired());

        maintenance.setCalibrationDate(dto.getCalibrationDate());
        maintenance.setNextCalibrationDate(dto.getNextCalibrationDate());

        maintenance.setCertificateNumber(dto.getCertificateNumber());

        maintenance.setRemarks(dto.getRemarks());

        maintenance.setStatus("PENDING");

        maintenance.setWorkOrderNumber(generateWorkOrder());

        maintenance.setCreatedAt(LocalDateTime.now());
        maintenance.setUpdatedAt(LocalDateTime.now());

        equipment.setStatus("UNDER_MAINTENANCE");
        equipmentRepository.save(equipment);

        Maintenance saved = maintenanceRepository.save(maintenance);

// =========================================================
// CREATE NOTIFICATION
// =========================================================

        Notification notification = new Notification();

        notification.setUserId(saved.getReportedBy());
        User user = userRepository
                .findById(saved.getReportedBy())
                .orElse(null);

        if (user != null) {

            emailService.sendEmail(
                    user.getEmail(),
                    "Maintenance Request Created",
                    "Your maintenance request has been created successfully.\n\n"
                            + "Work Order: " + saved.getWorkOrderNumber()
            );
        }

        notification.setTitle("Maintenance Request Created");

        notification.setMessage(
                "Work Order " +
                        saved.getWorkOrderNumber() +
                        " has been created successfully.");

        notification.setNotificationType("MAINTENANCE");

        notification.setReferenceId(saved.getMaintenanceId());

        notification.setIsRead(false);

        notificationRepository.save(notification);
        notificationWebSocketService.sendNotification(

                saved.getReportedBy(),

                "Maintenance Created",

                "Maintenance request has been created.",

                "MAINTENANCE"

        );
        AuditLogRequestDTO audit = new AuditLogRequestDTO();

        audit.setUserId(saved.getReportedBy());

        audit.setAction("CREATE");

        audit.setModule("MAINTENANCE");

        audit.setDescription(
                "Maintenance Request Created : "
                        + saved.getWorkOrderNumber());

        audit.setIpAddress("SYSTEM");

        auditLogService.saveAuditLog(audit);

        return map(saved);
    }

    // =========================================================
    // GENERATE WORK ORDER
    // =========================================================

    private String generateWorkOrder() {

        return "WO-" +
                LocalDate.now().getYear() +
                "-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0,8)
                        .toUpperCase();
    }
    // =========================================================
    // START MAINTENANCE
    // =========================================================

    public MaintenanceResponseDTO startMaintenance(Integer id) {

        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maintenance not found"));

        if ("COMPLETED".equalsIgnoreCase(maintenance.getStatus())) {
            throw new RuntimeException("Maintenance already completed.");
        }

        if ("IN_PROGRESS".equalsIgnoreCase(maintenance.getStatus())) {
            throw new RuntimeException("Maintenance already in progress.");
        }

        maintenance.setStatus("IN_PROGRESS");
        maintenance.setStartedDate(LocalDate.now());
        maintenance.setUpdatedAt(LocalDateTime.now());

        Maintenance updated = maintenanceRepository.save(maintenance);

        Notification notification = new Notification();

        notification.setUserId(updated.getReportedBy());

        notification.setTitle("Maintenance Started");

        notification.setMessage(
                "Maintenance work has started for Work Order : "
                        + updated.getWorkOrderNumber());

        notification.setNotificationType("MAINTENANCE");

        notification.setReferenceId(updated.getMaintenanceId());

        notification.setIsRead(false);

        notificationRepository.save(notification);
        User user = userRepository
                .findById(updated.getReportedBy())
                .orElse(null);

        if (user != null) {

            emailService.sendEmail(
                    user.getEmail(),
                    "Maintenance Started",
                    "Maintenance work has started.\n\n"
                            + "Work Order: " + updated.getWorkOrderNumber()
            );
        }notificationWebSocketService.sendNotification(

                updated.getReportedBy(),

                "Maintenance Started",

                "Maintenance work has started.",

                "MAINTENANCE"

        );
        AuditLogRequestDTO audit = new AuditLogRequestDTO();

        audit.setUserId(updated.getReportedBy());

        audit.setAction("START");

        audit.setModule("MAINTENANCE");

        audit.setDescription(
                "Maintenance Started : "
                        + updated.getWorkOrderNumber());

        audit.setIpAddress("SYSTEM");

        auditLogService.saveAuditLog(audit);
        return map(updated);
    }

    // =========================================================
    // GET ALL MAINTENANCE
    // =========================================================

    public List<MaintenanceResponseDTO> getAll() {

        return maintenanceRepository.findAll()
                .stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    public MaintenanceResponseDTO getById(Integer id) {

        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maintenance not found"));

        return map(maintenance);
    }

    // =========================================================
    // GET BY EQUIPMENT
    // =========================================================

    public List<MaintenanceResponseDTO> getByEquipment(Integer equipmentId) {

        return maintenanceRepository.findByEquipmentId(equipmentId)
                .stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET BY TECHNICIAN
    // =========================================================

    public List<MaintenanceResponseDTO> getByTechnician(Integer technicianId) {

        return maintenanceRepository.findByTechnicianId(technicianId)
                .stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET BY STATUS
    // =========================================================

    public List<MaintenanceResponseDTO> getByStatus(String status) {

        return maintenanceRepository.findByStatus(status)
                .stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET BY PRIORITY
    // =========================================================

    public List<MaintenanceResponseDTO> getByPriority(String priority) {

        return maintenanceRepository.findByPriority(priority)
                .stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET CALIBRATION PENDING
    // =========================================================

    public List<MaintenanceResponseDTO> getCalibrationPending() {

        return maintenanceRepository
                .findByCalibrationRequired(true)
                .stream()
                .map(this::map)
                .collect(Collectors.toList());
    }
    // =========================================================
    // COMPLETE MAINTENANCE
    // =========================================================

    public MaintenanceResponseDTO complete(Integer id, String remarks) {

        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maintenance not found"));

        Equipment equipment = equipmentRepository.findById(maintenance.getEquipmentId())
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        if ("COMPLETED".equalsIgnoreCase(maintenance.getStatus())) {
            throw new RuntimeException("Maintenance already completed.");
        }

        maintenance.setStatus("COMPLETED");
        maintenance.setCompletedDate(LocalDate.now());
        maintenance.setRemarks(remarks);
        maintenance.setUpdatedAt(LocalDateTime.now());

        if (maintenance.getStartedDate() != null) {

            int hours = (int) java.time.Duration.between(
                            maintenance.getStartedDate().atStartOfDay(),
                            LocalDate.now().atStartOfDay())
                    .toHours();

            maintenance.setDowntimeHours(hours);
        }

        equipment.setStatus("AVAILABLE");

        equipmentRepository.save(equipment);

        Maintenance updated = maintenanceRepository.save(maintenance);

        Notification notification = new Notification();

        notification.setUserId(updated.getReportedBy());

        notification.setTitle("Maintenance Completed");

        notification.setMessage(
                "Maintenance completed successfully for Work Order : "
                        + updated.getWorkOrderNumber());

        notification.setNotificationType("MAINTENANCE");

        notification.setReferenceId(updated.getMaintenanceId());

        notification.setIsRead(false);

        notificationRepository.save(notification);
        User user = userRepository
                .findById(updated.getReportedBy())
                .orElse(null);

        if (user != null) {

            emailService.sendEmail(
                    user.getEmail(),
                    "Maintenance Completed",
                    "Maintenance completed successfully.\n\n"
                            + "Work Order: " + updated.getWorkOrderNumber()
            );
        }
        notificationWebSocketService.sendNotification(

                updated.getReportedBy(),

                "Maintenance Completed",

                "Maintenance completed successfully.",

                "MAINTENANCE"

        );
        AuditLogRequestDTO audit = new AuditLogRequestDTO();

        audit.setUserId(updated.getReportedBy());

        audit.setAction("COMPLETE");

        audit.setModule("MAINTENANCE");

        audit.setDescription(
                "Maintenance Completed : "
                        + updated.getWorkOrderNumber());

        audit.setIpAddress("SYSTEM");

        auditLogService.saveAuditLog(audit);
        return map(updated);
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    public long totalMaintenance() {
        return maintenanceRepository.count();
    }

    public long pendingMaintenance() {
        return maintenanceRepository.countByStatus("PENDING");
    }

    public long inProgressMaintenance() {
        return maintenanceRepository.countByStatus("IN_PROGRESS");
    }

    public long completedMaintenance() {
        return maintenanceRepository.countByStatus("COMPLETED");
    }

    public long preventiveMaintenance() {
        return maintenanceRepository.countByMaintenanceType("PREVENTIVE");
    }

    public long correctiveMaintenance() {
        return maintenanceRepository.countByMaintenanceType("CORRECTIVE");
    }

    public long calibrationMaintenance() {
        return maintenanceRepository.countByCalibrationRequired(true);
    }

    // =========================================================
    // DTO MAPPER
    // =========================================================

    private MaintenanceResponseDTO map(Maintenance maintenance) {

        return new MaintenanceResponseDTO(

                maintenance.getMaintenanceId(),

                maintenance.getEquipmentId(),

                maintenance.getTechnicianId(),

                maintenance.getReportedBy(),

                maintenance.getWorkOrderNumber(),

                maintenance.getIssue(),

                maintenance.getIssueDescription(),

                maintenance.getMaintenanceType(),

                maintenance.getPriority(),

                maintenance.getStatus(),

                maintenance.getScheduledDate(),

                maintenance.getStartedDate(),

                maintenance.getEstimatedCompletionDate(),

                maintenance.getCompletedDate(),

                maintenance.getDowntimeHours(),

                maintenance.getCalibrationRequired(),

                maintenance.getCalibrationDate(),

                maintenance.getNextCalibrationDate(),

                maintenance.getCertificateNumber(),

                maintenance.getRemarks(),

                maintenance.getCreatedAt(),

                maintenance.getUpdatedAt()
        );
    }

}