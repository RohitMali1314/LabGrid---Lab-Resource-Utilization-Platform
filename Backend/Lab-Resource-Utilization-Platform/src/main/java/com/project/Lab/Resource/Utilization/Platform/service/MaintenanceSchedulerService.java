package com.project.Lab.Resource.Utilization.Platform.service;

import com.project.Lab.Resource.Utilization.Platform.dto.NotificationEvent;
import com.project.Lab.Resource.Utilization.Platform.entity.Maintenance;
import com.project.Lab.Resource.Utilization.Platform.entity.User;
import com.project.Lab.Resource.Utilization.Platform.repository.MaintenanceRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MaintenanceSchedulerService {

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KafkaNotificationProducer kafkaNotificationProducer;

    // =========================================================
    // DAILY MAINTENANCE / CALIBRATION NOTIFICATION SCHEDULER
    // Runs every day at 9:00 AM
    // =========================================================

    @Scheduled(cron = "0 0 9 * * ?")
    public void maintenanceReminderScheduler() {

        LocalDate today = LocalDate.now();

        System.out.println(
                "Starting maintenance notification scheduler for: "
                        + today
        );

        checkMaintenanceDue(today);

        checkOverdueMaintenance(today);

        checkCalibrationDue(today);

        checkCalibrationOverdue(today);

        System.out.println(
                "Maintenance notification scheduler completed for: "
                        + today
        );
    }

    // =========================================================
    // MAINTENANCE DUE TODAY
    // =========================================================

    private void checkMaintenanceDue(LocalDate today) {

        List<Maintenance> list =
                maintenanceRepository
                        .findByScheduledDateAndStatus(
                                today,
                                "PENDING"
                        );

        for (Maintenance maintenance : list) {

            publishNotification(
                    maintenance,
                    "Maintenance Due",
                    "Maintenance is scheduled for today.",
                    "MAINTENANCE"
            );
        }
    }

    // =========================================================
    // OVERDUE MAINTENANCE
    // =========================================================

    private void checkOverdueMaintenance(LocalDate today) {

        List<Maintenance> list =
                maintenanceRepository
                        .findByScheduledDateBeforeAndStatus(
                                today,
                                "PENDING"
                        );

        for (Maintenance maintenance : list) {

            publishNotification(
                    maintenance,
                    "Maintenance Overdue",
                    "Scheduled maintenance is overdue.",
                    "MAINTENANCE"
            );
        }
    }

    // =========================================================
    // CALIBRATION DUE TODAY
    // =========================================================

    private void checkCalibrationDue(LocalDate today) {

        List<Maintenance> list =
                maintenanceRepository
                        .findByNextCalibrationDateAndCalibrationRequired(
                                today,
                                true
                        );

        for (Maintenance maintenance : list) {

            publishNotification(
                    maintenance,
                    "Calibration Due",
                    "Equipment calibration is due today.",
                    "CALIBRATION"
            );
        }
    }

    // =========================================================
    // OVERDUE CALIBRATION
    // =========================================================

    private void checkCalibrationOverdue(LocalDate today) {

        List<Maintenance> list =
                maintenanceRepository
                        .findByNextCalibrationDateBeforeAndCalibrationRequired(
                                today,
                                true
                        );

        for (Maintenance maintenance : list) {

            publishNotification(
                    maintenance,
                    "Calibration Overdue",
                    "Equipment calibration is overdue.",
                    "CALIBRATION"
            );
        }
    }

    // =========================================================
    // PUBLISH NOTIFICATION EVENT TO KAFKA
    // =========================================================

    private void publishNotification(
            Maintenance maintenance,
            String title,
            String message,
            String notificationType
    ) {

        // -----------------------------------------------------
        // Determine recipient
        // -----------------------------------------------------

        Integer userId = maintenance.getReportedBy();

        if (userId == null) {

            userId = maintenance.getTechnicianId();
        }

        if (userId == null) {

            System.err.println(
                    "Notification skipped: no recipient found. "
                            + "Maintenance ID: "
                            + maintenance.getMaintenanceId()
            );

            return;
        }

        // -----------------------------------------------------
        // Resolve recipient email
        // -----------------------------------------------------

        String recipientEmail = findUserEmail(userId);

        // -----------------------------------------------------
        // Create Kafka event
        // -----------------------------------------------------

        NotificationEvent event =
                new NotificationEvent(

                        userId,

                        title,

                        message,

                        notificationType,

                        maintenance.getMaintenanceId(),

                        recipientEmail
                );

        // -----------------------------------------------------
        // Publish event
        // -----------------------------------------------------

        try {

            kafkaNotificationProducer.publish(event);

            System.out.println(
                    "Kafka notification published successfully. "
                            + "Type: "
                            + notificationType
                            + " | Title: "
                            + title
                            + " | Maintenance ID: "
                            + maintenance.getMaintenanceId()
                            + " | User ID: "
                            + userId
            );

        } catch (Exception e) {

            System.err.println(
                    "Failed to publish Kafka notification. "
                            + "Maintenance ID: "
                            + maintenance.getMaintenanceId()
                            + " | Error: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // FIND USER EMAIL
    // =========================================================

    private String findUserEmail(Integer userId) {

        try {

            User user =
                    userRepository.findById(userId)
                            .orElse(null);

            if (user == null) {

                System.err.println(
                        "User not found for notification. "
                                + "User ID: "
                                + userId
                );

                return null;
            }

            return user.getEmail();

        } catch (Exception e) {

            System.err.println(
                    "Unable to retrieve user email. "
                            + "User ID: "
                            + userId
                            + " | Error: "
                            + e.getMessage()
            );

            return null;
        }
    }
}