package com.project.Lab.Resource.Utilization.Platform.service;

import com.project.Lab.Resource.Utilization.Platform.dto.NotificationEvent;
import com.project.Lab.Resource.Utilization.Platform.dto.NotificationRequestDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.NotificationResponseDTO;
import com.project.Lab.Resource.Utilization.Platform.entity.Notification;
import com.project.Lab.Resource.Utilization.Platform.entity.User;
import com.project.Lab.Resource.Utilization.Platform.repository.NotificationRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationWebSocketService webSocketService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TwilioSmsService twilioSmsService;

    @Autowired
    private UserRepository userRepository;


    // =========================================================
    // CREATE NOTIFICATION - NORMAL API
    // =========================================================

    public NotificationResponseDTO create(
            NotificationRequestDTO dto
    ) {

        Notification notification = new Notification();

        notification.setUserId(dto.getUserId());
        notification.setTitle(dto.getTitle());
        notification.setMessage(dto.getMessage());
        notification.setNotificationType(
                dto.getNotificationType()
        );
        notification.setReferenceId(
                dto.getReferenceId()
        );
        notification.setIsRead(false);

        Notification saved =
                notificationRepository.save(notification);

        return map(saved);
    }


    // =========================================================
    // KAFKA NOTIFICATION PROCESSING
    // =========================================================

    public void processKafkaNotification(
            NotificationEvent event
    ) {

        // -----------------------------------------------------
        // 1. SAVE NOTIFICATION TO DATABASE
        // -----------------------------------------------------

        Notification notification =
                new Notification();

        notification.setUserId(
                event.getUserId()
        );

        notification.setTitle(
                event.getTitle()
        );

        notification.setMessage(
                event.getMessage()
        );

        notification.setNotificationType(
                event.getNotificationType()
        );

        notification.setReferenceId(
                event.getReferenceId()
        );

        notification.setIsRead(false);

        Notification saved =
                notificationRepository.save(notification);


        // -----------------------------------------------------
        // 2. WEBSOCKET REAL-TIME NOTIFICATION
        // -----------------------------------------------------

        try {

            webSocketService.sendNotification(

                    saved.getUserId(),

                    saved.getTitle(),

                    saved.getMessage(),

                    saved.getNotificationType()

            );

            System.out.println(
                    "WebSocket notification sent successfully."
            );

        } catch (Exception e) {

            System.err.println(
                    "WebSocket notification failed: "
                            + e.getMessage()
            );
        }


        // -----------------------------------------------------
        // 3. FIND USER DETAILS
        // -----------------------------------------------------

        User user = null;

        try {

            user =
                    userRepository.findById(
                            event.getUserId()
                    ).orElse(null);

        } catch (Exception e) {

            System.err.println(
                    "Unable to find notification user: "
                            + e.getMessage()
            );
        }


        // -----------------------------------------------------
        // 4. EMAIL NOTIFICATION
        // -----------------------------------------------------

        String recipientEmail =
                event.getRecipientEmail();

        if ((recipientEmail == null ||
                recipientEmail.isBlank())
                && user != null) {

            recipientEmail =
                    user.getEmail();
        }

        if (recipientEmail != null &&
                !recipientEmail.isBlank()) {

            try {

                emailService.sendEmail(

                        recipientEmail,

                        event.getTitle(),

                        event.getMessage()

                );

                System.out.println(
                        "Email notification sent successfully to: "
                                + recipientEmail
                );

            } catch (Exception e) {

                System.err.println(
                        "Email notification failed: "
                                + e.getMessage()
                );
            }

        } else {

            System.out.println(
                    "Email notification skipped: "
                            + "recipient email not available."
            );
        }


        // -----------------------------------------------------
        // 5. TWILIO SMS NOTIFICATION
        // -----------------------------------------------------

        String recipientPhone = null;

        if (user != null) {

            recipientPhone =
                    user.getPhone();
        }

        if (recipientPhone != null &&
                !recipientPhone.isBlank()) {

            String smsMessage =
                    "LabGrid Alert: "
                            + event.getTitle()
                            + " - "
                            + event.getMessage();

            try {

                boolean smsSent =
                        twilioSmsService.sendSms(
                                recipientPhone,
                                smsMessage
                        );

                if (smsSent) {

                    System.out.println(
                            "SMS notification sent successfully to: "
                                    + recipientPhone
                    );

                } else {

                    System.out.println(
                            "SMS notification was not sent."
                    );
                }

            } catch (Exception e) {

                System.err.println(
                        "SMS notification failed: "
                                + e.getMessage()
                );
            }

        } else {

            System.out.println(
                    "SMS notification skipped: "
                            + "user phone number not available."
            );
        }
    }


    // =========================================================
    // GET ALL
    // =========================================================

    public List<NotificationResponseDTO> getAll() {

        return notificationRepository.findAll()

                .stream()

                .map(this::map)

                .collect(Collectors.toList());
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    public NotificationResponseDTO getById(
            Integer id
    ) {

        Notification notification =
                notificationRepository.findById(id)

                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification not found"
                                )
                        );

        return map(notification);
    }


    // =========================================================
    // GET USER NOTIFICATIONS
    // =========================================================

    public List<NotificationResponseDTO> getByUser(
            Integer userId
    ) {

        return notificationRepository

                .findByUserIdOrderByCreatedAtDesc(userId)

                .stream()

                .map(this::map)

                .collect(Collectors.toList());
    }


    // =========================================================
    // GET UNREAD
    // =========================================================

    public List<NotificationResponseDTO> getUnread(
            Integer userId
    ) {

        return notificationRepository

                .findByUserIdAndIsRead(
                        userId,
                        false
                )

                .stream()

                .map(this::map)

                .collect(Collectors.toList());
    }


    // =========================================================
    // MARK AS READ
    // =========================================================

    public NotificationResponseDTO markAsRead(
            Integer id
    ) {

        Notification notification =
                notificationRepository.findById(id)

                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification not found"
                                )
                        );

        notification.setIsRead(true);

        Notification updated =
                notificationRepository.save(notification);

        return map(updated);
    }


    // =========================================================
    // DELETE
    // =========================================================

    public void delete(
            Integer id
    ) {

        Notification notification =
                notificationRepository.findById(id)

                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification not found"
                                )
                        );

        notificationRepository.delete(notification);
    }


    // =========================================================
    // UNREAD COUNT
    // =========================================================

    public long unreadCount(
            Integer userId
    ) {

        return notificationRepository

                .countByUserIdAndIsRead(
                        userId,
                        false
                );
    }


    // =========================================================
    // DTO MAPPER
    // =========================================================

    private NotificationResponseDTO map(
            Notification notification
    ) {

        return new NotificationResponseDTO(

                notification.getNotificationId(),

                notification.getUserId(),

                notification.getTitle(),

                notification.getMessage(),

                notification.getNotificationType(),

                notification.getReferenceId(),

                notification.getIsRead(),

                notification.getCreatedAt()
        );
    }
}