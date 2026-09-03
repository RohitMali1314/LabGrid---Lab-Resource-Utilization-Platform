package com.project.Lab.Resource.Utilization.Platform.controller;

import com.project.Lab.Resource.Utilization.Platform.dto.AuditLogResponseDTO;
import com.project.Lab.Resource.Utilization.Platform.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin("*")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    // =========================================================
    // GET ALL AUDIT LOGS
    // SYSTEM ADMIN ONLY
    // =========================================================

    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public List<AuditLogResponseDTO> getAllLogs() {

        return auditLogService.getAllLogs();

    }

    // =========================================================
    // GET BY USER
    // =========================================================

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public List<AuditLogResponseDTO> getByUser(
            @PathVariable Integer userId) {

        return auditLogService.getByUser(userId);

    }

    // =========================================================
    // GET BY MODULE
    // =========================================================

    @GetMapping("/module/{module}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public List<AuditLogResponseDTO> getByModule(
            @PathVariable String module) {

        return auditLogService.getByModule(module);

    }

    // =========================================================
    // GET BY ACTION
    // =========================================================

    @GetMapping("/action/{action}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public List<AuditLogResponseDTO> getByAction(
            @PathVariable String action) {

        return auditLogService.getByAction(action);

    }

}