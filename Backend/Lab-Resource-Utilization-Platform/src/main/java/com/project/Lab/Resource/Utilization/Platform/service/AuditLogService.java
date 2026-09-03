package com.project.Lab.Resource.Utilization.Platform.service;

import com.project.Lab.Resource.Utilization.Platform.dto.AuditLogRequestDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.AuditLogResponseDTO;
import com.project.Lab.Resource.Utilization.Platform.entity.AuditLog;
import com.project.Lab.Resource.Utilization.Platform.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    // =========================================================
    // SAVE AUDIT LOG
    // =========================================================

    public void saveAuditLog(AuditLogRequestDTO dto) {

        AuditLog auditLog = new AuditLog();

        auditLog.setUserId(dto.getUserId());

        auditLog.setAction(dto.getAction());

        auditLog.setModule(dto.getModule());

        auditLog.setDescription(dto.getDescription());

        auditLog.setIpAddress(dto.getIpAddress());

        auditLogRepository.save(auditLog);

    }

    // =========================================================
    // GET ALL LOGS
    // =========================================================

    public List<AuditLogResponseDTO> getAllLogs() {

        return auditLogRepository.findAll()
                .stream()
                .map(this::map)
                .collect(Collectors.toList());

    }

    // =========================================================
    // GET LOG BY ID
    // =========================================================

    public AuditLogResponseDTO getById(Integer id) {

        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Audit Log not found"));

        return map(auditLog);

    }

    // =========================================================
    // GET BY USER
    // =========================================================

    public List<AuditLogResponseDTO> getByUser(Integer userId) {

        return auditLogRepository.findByUserId(userId)
                .stream()
                .map(this::map)
                .collect(Collectors.toList());

    }

    // =========================================================
    // GET BY MODULE
    // =========================================================

    public List<AuditLogResponseDTO> getByModule(String module) {

        return auditLogRepository.findByModule(module)
                .stream()
                .map(this::map)
                .collect(Collectors.toList());

    }

    // =========================================================
    // GET BY ACTION
    // =========================================================

    public List<AuditLogResponseDTO> getByAction(String action) {

        return auditLogRepository.findByAction(action)
                .stream()
                .map(this::map)
                .collect(Collectors.toList());

    }

    // =========================================================
    // DELETE LOG
    // =========================================================

    public void delete(Integer id) {

        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Audit Log not found"));

        auditLogRepository.delete(auditLog);

    }

    // =========================================================
    // DTO MAPPER
    // =========================================================

    private AuditLogResponseDTO map(AuditLog auditLog) {

        return new AuditLogResponseDTO(

                auditLog.getAuditId(),

                auditLog.getUserId(),

                auditLog.getAction(),

                auditLog.getModule(),

                auditLog.getDescription(),

                auditLog.getIpAddress(),

                auditLog.getCreatedAt()

        );

    }

}