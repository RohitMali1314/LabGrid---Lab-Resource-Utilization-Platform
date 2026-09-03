package com.project.Lab.Resource.Utilization.Platform.dto;

import java.time.LocalDateTime;

public class NotificationMessage {

    private Integer userId;

    private String title;

    private String message;

    private String type;

    private LocalDateTime time;

    public NotificationMessage() {
    }

    public NotificationMessage(
            Integer userId,
            String title,
            String message,
            String type,
            LocalDateTime time) {

        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.time = time;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

}