package com.project.Lab.Resource.Utilization.Platform.service;

import com.project.Lab.Resource.Utilization.Platform.dto.ResourceShareRequestDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.ResourceShareResponseDTO;
import com.project.Lab.Resource.Utilization.Platform.entity.ResourceShare;
import com.project.Lab.Resource.Utilization.Platform.entity.User;
import com.project.Lab.Resource.Utilization.Platform.repository.ResourceShareRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResourceShareService {

    @Autowired
    private ResourceShareRepository resourceShareRepository;

    @Autowired
    private UserRepository userRepository;

    // =========================================================
    // CREATE SHARE
    // =========================================================

    @Transactional
    public ResourceShareResponseDTO createShare(
            ResourceShareRequestDTO dto,
            String email
    ) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );

        if (user.getInstitutionId() == null) {
            throw new RuntimeException(
                    "User is not associated with an institution"
            );
        }

        if (dto.getEquipmentId() == null) {
            throw new RuntimeException(
                    "Equipment ID is required"
            );
        }

        if (dto.getTargetInstitutionId() == null) {
            throw new RuntimeException(
                    "Target institution ID is required"
            );
        }

        if (dto.getStartDate() == null ||
                dto.getEndDate() == null) {

            throw new RuntimeException(
                    "Start date and end date are required"
            );
        }

        if (dto.getEndDate()
                .isBefore(dto.getStartDate())) {

            throw new RuntimeException(
                    "End date cannot be before start date"
            );
        }

        if (user.getInstitutionId()
                .equals(
                        dto.getTargetInstitutionId()
                )) {

            throw new RuntimeException(
                    "Owner and target institution cannot be the same"
            );
        }

        boolean exists =
                resourceShareRepository
                        .existsByEquipmentIdAndTargetInstitutionIdAndStatus(
                                dto.getEquipmentId(),
                                dto.getTargetInstitutionId(),
                                "ACTIVE"
                        );

        if (exists) {
            throw new RuntimeException(
                    "This equipment is already actively shared with the target institution"
            );
        }

        ResourceShare share =
                new ResourceShare();

        share.setEquipmentId(
                dto.getEquipmentId()
        );

        share.setOwnerInstitutionId(
                user.getInstitutionId()
        );

        share.setTargetInstitutionId(
                dto.getTargetInstitutionId()
        );

        share.setRequestedBy(
                user.getUserId()
        );

        share.setStatus(
                "PENDING"
        );

        share.setStartDate(
                dto.getStartDate()
        );

        share.setEndDate(
                dto.getEndDate()
        );

        share.setSharingRate(
                dto.getSharingRate()
        );

        share.setRemarks(
                dto.getRemarks()
        );

        share.setCreatedAt(
                LocalDateTime.now()
        );

        share.setUpdatedAt(
                LocalDateTime.now()
        );

        ResourceShare saved =
                resourceShareRepository.save(
                        share
                );

        return convertToResponse(saved);
    }

    // =========================================================
    // GET ALL
    // =========================================================

    public List<ResourceShareResponseDTO> getAll() {

        return resourceShareRepository
                .findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    public ResourceShareResponseDTO getById(
            Integer id
    ) {

        ResourceShare share =
                resourceShareRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Resource share not found"
                                )
                        );

        return convertToResponse(share);
    }

    // =========================================================
    // GET BY EQUIPMENT
    // =========================================================

    public List<ResourceShareResponseDTO> getByEquipment(
            Integer equipmentId
    ) {

        return resourceShareRepository
                .findByEquipmentId(equipmentId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // =========================================================
    // GET BY OWNER
    // =========================================================

    public List<ResourceShareResponseDTO>
    getByOwnerInstitution(
            Integer institutionId
    ) {

        return resourceShareRepository
                .findByOwnerInstitutionId(
                        institutionId
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // =========================================================
    // GET BY TARGET
    // =========================================================

    public List<ResourceShareResponseDTO>
    getByTargetInstitution(
            Integer institutionId
    ) {

        return resourceShareRepository
                .findByTargetInstitutionId(
                        institutionId
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // =========================================================
    // GET BY STATUS
    // =========================================================

    public List<ResourceShareResponseDTO> getByStatus(
            String status
    ) {

        return resourceShareRepository
                .findByStatus(
                        status.toUpperCase()
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // =========================================================
    // APPROVE
    // =========================================================

    @Transactional
    public ResourceShareResponseDTO approve(
            Integer id,
            String email
    ) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );

        ResourceShare share =
                resourceShareRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Resource share not found"
                                )
                        );

        if (!"PENDING".equalsIgnoreCase(
                share.getStatus()
        )) {

            throw new RuntimeException(
                    "Only pending sharing requests can be approved"
            );
        }

        share.setStatus(
                "APPROVED"
        );

        share.setApprovedBy(
                user.getUserId()
        );

        share.setUpdatedAt(
                LocalDateTime.now()
        );

        return convertToResponse(
                resourceShareRepository.save(
                        share
                )
        );
    }

    // =========================================================
    // REJECT
    // =========================================================

    @Transactional
    public ResourceShareResponseDTO reject(
            Integer id,
            String email,
            String remarks
    ) {

        ResourceShare share =
                resourceShareRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Resource share not found"
                                )
                        );

        if (!"PENDING".equalsIgnoreCase(
                share.getStatus()
        )) {

            throw new RuntimeException(
                    "Only pending sharing requests can be rejected"
            );
        }

        share.setStatus(
                "REJECTED"
        );

        if (remarks != null &&
                !remarks.isBlank()) {

            share.setRemarks(
                    remarks
            );
        }

        share.setUpdatedAt(
                LocalDateTime.now()
        );

        return convertToResponse(
                resourceShareRepository.save(
                        share
                )
        );
    }

    // =========================================================
    // ACTIVATE
    // =========================================================

    @Transactional
    public ResourceShareResponseDTO activate(
            Integer id,
            String email
    ) {

        ResourceShare share =
                resourceShareRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Resource share not found"
                                )
                        );

        if (!"APPROVED".equalsIgnoreCase(
                share.getStatus()
        )) {

            throw new RuntimeException(
                    "Only approved resource shares can be activated"
            );
        }

        if (share.getStartDate() != null &&
                share.getEndDate() != null &&
                share.getEndDate()
                        .isBefore(
                                share.getStartDate()
                        )) {

            throw new RuntimeException(
                    "Invalid sharing date range"
            );
        }

        share.setStatus(
                "ACTIVE"
        );

        share.setUpdatedAt(
                LocalDateTime.now()
        );

        return convertToResponse(
                resourceShareRepository.save(
                        share
                )
        );
    }

    // =========================================================
    // CANCEL
    // =========================================================

    @Transactional
    public ResourceShareResponseDTO cancel(
            Integer id,
            String email
    ) {

        ResourceShare share =
                resourceShareRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Resource share not found"
                                )
                        );

        if ("CANCELLED".equalsIgnoreCase(
                share.getStatus()
        )) {

            throw new RuntimeException(
                    "Resource share is already cancelled"
            );
        }

        share.setStatus(
                "CANCELLED"
        );

        share.setUpdatedAt(
                LocalDateTime.now()
        );

        return convertToResponse(
                resourceShareRepository.save(
                        share
                )
        );
    }

    // =========================================================
    // ACTIVE SHARES FOR EQUIPMENT
    // =========================================================

    public List<ResourceShareResponseDTO>
    getActiveSharesForEquipment(
            Integer equipmentId
    ) {

        return resourceShareRepository
                .findByEquipmentIdAndStatusOrderByStartDateAsc(
                        equipmentId,
                        "ACTIVE"
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // =========================================================
    // CONVERT ENTITY → DTO
    // =========================================================

    private ResourceShareResponseDTO convertToResponse(
            ResourceShare share
    ) {

        ResourceShareResponseDTO dto =
                new ResourceShareResponseDTO();

        dto.setShareId(
                share.getShareId()
        );

        dto.setEquipmentId(
                share.getEquipmentId()
        );

        dto.setOwnerInstitutionId(
                share.getOwnerInstitutionId()
        );

        dto.setTargetInstitutionId(
                share.getTargetInstitutionId()
        );

        dto.setRequestedBy(
                share.getRequestedBy()
        );

        dto.setApprovedBy(
                share.getApprovedBy()
        );

        dto.setStatus(
                share.getStatus()
        );

        dto.setStartDate(
                share.getStartDate()
        );

        dto.setEndDate(
                share.getEndDate()
        );

        dto.setSharingRate(
                share.getSharingRate()
        );

        dto.setRemarks(
                share.getRemarks()
        );

        dto.setCreatedAt(
                share.getCreatedAt()
        );

        dto.setUpdatedAt(
                share.getUpdatedAt()
        );

        return dto;
    }
}