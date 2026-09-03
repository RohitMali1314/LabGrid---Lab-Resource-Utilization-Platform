package com.project.Lab.Resource.Utilization.Platform.service;

import com.project.Lab.Resource.Utilization.Platform.dto.NotificationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationWebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendNotification(

            Integer userId,

            String title,

            String message,

            String type

    ) {

        NotificationMessage notification =
                new NotificationMessage(

                        userId,

                        title,

                        message,

                        type,

                        LocalDateTime.now()

                );

        messagingTemplate.convertAndSend(

                "/topic/notifications/" + userId,

                notification

        );

    }

}