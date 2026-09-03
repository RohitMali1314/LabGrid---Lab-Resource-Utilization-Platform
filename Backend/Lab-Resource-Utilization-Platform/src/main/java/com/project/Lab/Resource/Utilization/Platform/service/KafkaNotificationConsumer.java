package com.project.Lab.Resource.Utilization.Platform.service;

import com.project.Lab.Resource.Utilization.Platform.dto.NotificationEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaNotificationConsumer {

    private final NotificationService notificationService;

    public KafkaNotificationConsumer(
            NotificationService notificationService
    ) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = "labgrid-notifications",
            groupId = "lab-resource-notification-group"
    )
    public void consume(NotificationEvent event) {

        System.out.println(
                "Kafka notification received for user: "
                        + event.getUserId()
        );

        notificationService.processKafkaNotification(event);
    }
}