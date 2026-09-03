package com.project.Lab.Resource.Utilization.Platform.service;

import com.project.Lab.Resource.Utilization.Platform.dto.NotificationEvent;
import com.project.Lab.Resource.Utilization.Platform.security.KafkaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaNotificationProducer {

    @Autowired
    private KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public void publish(NotificationEvent event) {

        String key = String.valueOf(event.getUserId());

        kafkaTemplate.send(
                KafkaConfig.NOTIFICATION_TOPIC,
                key,
                event
        );
    }
}