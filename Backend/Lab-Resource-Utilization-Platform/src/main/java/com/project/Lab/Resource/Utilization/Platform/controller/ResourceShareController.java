package com.project.Lab.Resource.Utilization.Platform.controller;

import com.project.Lab.Resource.Utilization.Platform.dto.ResourceShareRequestDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.ResourceShareResponseDTO;
import com.project.Lab.Resource.Utilization.Platform.service.ResourceShareService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resource-sharing")
public class ResourceShareController {

    @Autowired
    private ResourceShareService resourceShareService;

    // =========================================================
    // CREATE SHARING REQUEST
    // Institution Admin & System Admin
    // =========================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','SYSTEM_ADMIN')")
    public ResourceShareResponseDTO create(
            @RequestBody ResourceShareRequestDTO dto,
            Authentication authentication
    ) {

        return resourceShareService.createShare(
                dto,
                authentication.getName()
        );
    }

    // =========================================================
    // GET ALL SHARES
    // Institution Admin, System Admin & Department Head
    // =========================================================

    @GetMapping
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','SYSTEM_ADMIN','DEPARTMENT_HEAD')")
    public List<ResourceShareResponseDTO> getAll() {

        return resourceShareService.getAll();
    }

    // =========================================================
    // GET SHARE BY ID
    // =========================================================

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResourceShareResponseDTO getById(
            @PathVariable Integer id
    ) {

        return resourceShareService.getById(id);
    }

    // =========================================================
    // GET SHARES BY EQUIPMENT
    // =========================================================

    @GetMapping("/equipment/{equipmentId}")
    @PreAuthorize("isAuthenticated()")
    public List<ResourceShareResponseDTO> getByEquipment(
            @PathVariable Integer equipmentId
    ) {

        return resourceShareService.getByEquipment(equipmentId);
    }

    // =========================================================
    // GET SHARES BY OWNER INSTITUTION
    // =========================================================

    @GetMapping("/owner/{institutionId}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','SYSTEM_ADMIN','DEPARTMENT_HEAD')")
    public List<ResourceShareResponseDTO> getByOwner(
            @PathVariable Integer institutionId
    ) {

        return resourceShareService.getByOwnerInstitution(
                institutionId
        );
    }

    // =========================================================
    // GET SHARES BY TARGET INSTITUTION
    // =========================================================

    @GetMapping("/target/{institutionId}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','SYSTEM_ADMIN','DEPARTMENT_HEAD')")
    public List<ResourceShareResponseDTO> getByTarget(
            @PathVariable Integer institutionId
    ) {

        return resourceShareService.getByTargetInstitution(
                institutionId
        );
    }

    // =========================================================
    // GET SHARES BY STATUS
    // =========================================================

    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public List<ResourceShareResponseDTO> getByStatus(
            @PathVariable String status
    ) {

        return resourceShareService.getByStatus(status);
    }

    // =========================================================
    // APPROVE SHARING REQUEST
    // =========================================================

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','SYSTEM_ADMIN')")
    public ResourceShareResponseDTO approve(
            @PathVariable Integer id,
            Authentication authentication
    ) {

        return resourceShareService.approve(
                id,
                authentication.getName()
        );
    }

    // =========================================================
    // REJECT SHARING REQUEST
    // =========================================================

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','SYSTEM_ADMIN')")
    public ResourceShareResponseDTO reject(
            @PathVariable Integer id,
            @RequestParam(required = false) String remarks,
            Authentication authentication
    ) {

        return resourceShareService.reject(
                id,
                authentication.getName(),
                remarks
        );
    }

    // =========================================================
    // ACTIVATE SHARING
    // =========================================================

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','SYSTEM_ADMIN')")
    public ResourceShareResponseDTO activate(
            @PathVariable Integer id,
            Authentication authentication
    ) {

        return resourceShareService.activate(
                id,
                authentication.getName()
        );
    }

    // =========================================================
    // CANCEL SHARING
    // =========================================================

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','SYSTEM_ADMIN')")
    public ResourceShareResponseDTO cancel(
            @PathVariable Integer id,
            Authentication authentication
    ) {

        return resourceShareService.cancel(
                id,
                authentication.getName()
        );
    }
}