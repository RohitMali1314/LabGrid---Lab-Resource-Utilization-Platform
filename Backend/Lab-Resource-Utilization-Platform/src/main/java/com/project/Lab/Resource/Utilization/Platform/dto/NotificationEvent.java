package com.project.Lab.Resource.Utilization.Platform.dto;

import java.io.Serializable;

public class NotificationEvent implements Serializable {

    private Integer userId;
    private String title;
    private String message;
    private String notificationType;
    private Integer referenceId;
    private String recipientEmail;

    public NotificationEvent() {
    }

    public NotificationEvent(
            Integer userId,
            String title,
            String message,
            String notificationType,
            Integer referenceId,
            String recipientEmail
    ) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.referenceId = referenceId;
        this.recipientEmail = recipientEmail;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public Integer getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Integer referenceId) {
        this.referenceId = referenceId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }
}