package com.project.Lab.Resource.Utilization.Platform.controller;

import com.project.Lab.Resource.Utilization.Platform.dto.EquipmentUsageResponseDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.EquipmentUtilizationDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.DepartmentUtilizationDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.PeakUsageDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.UtilizationSummaryDTO;
import com.project.Lab.Resource.Utilization.Platform.entity.EquipmentUsage;
import com.project.Lab.Resource.Utilization.Platform.service.EquipmentUsageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/utilization")
@CrossOrigin(origins = "*")
public class UtilizationController {

    @Autowired
    private EquipmentUsageService equipmentUsageService;


    // =========================================================
    // START EQUIPMENT USAGE
    // =========================================================

    @PostMapping("/usage/start")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EquipmentUsageResponseDTO> startUsage(
            @RequestParam Integer bookingId,
            @RequestParam Integer equipmentId,
            @RequestParam Integer userId
    ) {

        EquipmentUsage usage =
                equipmentUsageService.startUsage(
                        bookingId,
                        equipmentId,
                        userId
                );

        return ResponseEntity.ok(
                equipmentUsageService.toResponseDTO(
                        usage
                )
        );
    }


    // =========================================================
    // STOP EQUIPMENT USAGE
    // =========================================================

    @PutMapping("/usage/stop/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EquipmentUsageResponseDTO> stopUsage(
            @PathVariable Integer bookingId
    ) {

        EquipmentUsage usage =
                equipmentUsageService.stopUsage(
                        bookingId
                );

        return ResponseEntity.ok(
                equipmentUsageService.toResponseDTO(
                        usage
                )
        );
    }


    // =========================================================
    // GET USAGE BY ID
    // =========================================================

    @GetMapping("/usage/{usageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EquipmentUsageResponseDTO> getUsageById(
            @PathVariable Integer usageId
    ) {

        EquipmentUsage usage =
                equipmentUsageService.getUsageById(
                        usageId
                );

        return ResponseEntity.ok(
                equipmentUsageService.toResponseDTO(
                        usage
                )
        );
    }


    // =========================================================
    // GET USAGE BY BOOKING
    // =========================================================

    @GetMapping("/usage/booking/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EquipmentUsageResponseDTO>> getByBooking(
            @PathVariable Integer bookingId
    ) {

        return ResponseEntity.ok(
                equipmentUsageService
                        .getByBooking(bookingId)
                        .stream()
                        .map(equipmentUsageService::toResponseDTO)
                        .toList()
        );
    }


    // =========================================================
    // GET USAGE BY EQUIPMENT
    // =========================================================

    @GetMapping("/usage/equipment/{equipmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EquipmentUsageResponseDTO>> getByEquipment(
            @PathVariable Integer equipmentId
    ) {

        return ResponseEntity.ok(
                equipmentUsageService
                        .getByEquipment(equipmentId)
                        .stream()
                        .map(equipmentUsageService::toResponseDTO)
                        .toList()
        );
    }


    // =========================================================
    // GET USAGE BY USER
    // =========================================================

    @GetMapping("/usage/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EquipmentUsageResponseDTO>> getByUser(
            @PathVariable Integer userId
    ) {

        return ResponseEntity.ok(
                equipmentUsageService
                        .getByUser(userId)
                        .stream()
                        .map(equipmentUsageService::toResponseDTO)
                        .toList()
        );
    }


    // =========================================================
    // GET CURRENT ACTIVE USAGE
    // =========================================================

    @GetMapping("/usage/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EquipmentUsageResponseDTO>> getActiveUsage() {

        return ResponseEntity.ok(
                equipmentUsageService
                        .getActiveUsage()
                        .stream()
                        .map(equipmentUsageService::toResponseDTO)
                        .toList()
        );
    }


    // =========================================================
    // OVERALL UTILIZATION SUMMARY
    // =========================================================

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UtilizationSummaryDTO> getSummary(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end
    ) {

        return ResponseEntity.ok(
                equipmentUsageService
                        .getUtilizationSummary(
                                start,
                                end
                        )
        );
    }


    // =========================================================
    // EQUIPMENT-WISE UTILIZATION
    // =========================================================

    @GetMapping("/equipment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EquipmentUtilizationDTO>>
    getEquipmentUtilization(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end
    ) {

        return ResponseEntity.ok(
                equipmentUsageService
                        .getEquipmentUtilization(
                                start,
                                end
                        )
        );
    }


    // =========================================================
    // DEPARTMENT-WISE UTILIZATION
    // =========================================================

    @GetMapping("/department")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DepartmentUtilizationDTO>>
    getDepartmentUtilization(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end
    ) {

        return ResponseEntity.ok(
                equipmentUsageService
                        .getDepartmentUtilization(
                                start,
                                end
                        )
        );
    }


    // =========================================================
    // PEAK USAGE
    // =========================================================

    @GetMapping("/peak")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PeakUsageDTO>> getPeakUsage(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end
    ) {

        return ResponseEntity.ok(
                equipmentUsageService
                        .getPeakUsage(
                                start,
                                end
                        )
        );
    }


    // =========================================================
    // EQUIPMENT UTILIZATION PERCENTAGE
    // =========================================================

    @GetMapping("/equipment/{equipmentId}/percentage")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal>
    getEquipmentUtilizationPercentage(

            @PathVariable Integer equipmentId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end
    ) {

        return ResponseEntity.ok(
                equipmentUsageService
                        .getEquipmentUtilizationPercentage(
                                equipmentId,
                                start,
                                end
                        )
        );
    }


    // =========================================================
    // EQUIPMENT IDLE HOURS
    // =========================================================

    @GetMapping("/equipment/{equipmentId}/idle-hours")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> getIdleHours(

            @PathVariable Integer equipmentId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end
    ) {

        return ResponseEntity.ok(
                equipmentUsageService.getIdleHours(
                        equipmentId,
                        start,
                        end
                )
        );
    }


    // =========================================================
    // USAGE RECORDS FOR PERIOD
    // =========================================================

    @GetMapping("/usage")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EquipmentUsageResponseDTO>>
    getUsageForPeriod(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end
    ) {

        return ResponseEntity.ok(
                equipmentUsageService
                        .getUsageForPeriod(
                                start,
                                end
                        )
                        .stream()
                        .map(equipmentUsageService::toResponseDTO)
                        .toList()
        );
    }
}