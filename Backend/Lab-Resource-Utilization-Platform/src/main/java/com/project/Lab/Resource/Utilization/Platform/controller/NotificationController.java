package com.project.Lab.Resource.Utilization.Platform.controller;

import com.project.Lab.Resource.Utilization.Platform.dto.NotificationRequestDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.NotificationResponseDTO;
import com.project.Lab.Resource.Utilization.Platform.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin("*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // ==========================================================
    // CREATE NOTIFICATION
    // ==========================================================
    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','INSTITUTION_ADMIN','LAB_MANAGER','LAB_TECHNICIAN')")
    public NotificationResponseDTO create(
            @RequestBody NotificationRequestDTO dto) {

        return notificationService.create(dto);
    }

    // ==========================================================
    // GET ALL
    // ==========================================================
    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','INSTITUTION_ADMIN')")
    public List<NotificationResponseDTO> getAll() {

        return notificationService.getAll();
    }

    // ==========================================================
    // GET BY ID
    // ==========================================================
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public NotificationResponseDTO getById(
            @PathVariable Integer id) {

        return notificationService.getById(id);
    }

    // ==========================================================
    // GET USER NOTIFICATIONS
    // ==========================================================
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public List<NotificationResponseDTO> getByUser(
            @PathVariable Integer userId) {

        return notificationService.getByUser(userId);
    }

    // ==========================================================
    // GET UNREAD
    // ==========================================================
    @GetMapping("/user/{userId}/unread")
    @PreAuthorize("isAuthenticated()")
    public List<NotificationResponseDTO> unread(
            @PathVariable Integer userId) {

        return notificationService.getUnread(userId);
    }

    // ==========================================================
    // MARK AS READ
    // ==========================================================
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public NotificationResponseDTO markRead(
            @PathVariable Integer id) {

        return notificationService.markAsRead(id);
    }

    // ==========================================================
    // UNREAD COUNT
    // ==========================================================
    @GetMapping("/user/{userId}/count")
    @PreAuthorize("isAuthenticated()")
    public long unreadCount(
            @PathVariable Integer userId) {

        return notificationService.unreadCount(userId);
    }

    // ==========================================================
    // DELETE
    // ==========================================================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','INSTITUTION_ADMIN')")
    public String delete(
            @PathVariable Integer id) {

        notificationService.delete(id);

        return "Notification deleted successfully";
    }

}