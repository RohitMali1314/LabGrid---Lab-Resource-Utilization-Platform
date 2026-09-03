package com.project.Lab.Resource.Utilization.Platform.service;

import com.project.Lab.Resource.Utilization.Platform.dto.DashboardStatsDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.DepartmentStatDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.DemandAnalysisDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.HeatmapDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.MonthlyHeatmapDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.UtilizationPointDTO;
import com.project.Lab.Resource.Utilization.Platform.entity.Equipment;
import com.project.Lab.Resource.Utilization.Platform.repository.BookingRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.DepartmentRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.EquipmentRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import com.project.Lab.Resource.Utilization.Platform.dto.DepartmentDemandDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.DepartmentDemandDTO;
@Service
public class AnalyticsService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private UserRepository userRepository;


    // =========================================================
    // ROLE BASED DASHBOARD
    // =========================================================

    public DashboardStatsDTO getDashboardStats(String role) {

        role = role.toUpperCase();

        switch (role) {

            case "STUDENT":
            case "RESEARCHER":
            case "LAB_TECHNICIAN":
            case "LAB_MANAGER":
            case "DEPARTMENT_HEAD":
            case "INSTITUTION_ADMIN":
            case "SYSTEM_ADMIN":
                break;

            default:
                throw new RuntimeException(
                        "Invalid Role : " + role
                );
        }

        Long totalBookings =
                bookingRepository.count();

        Long pendingBookings =
                bookingRepository.countByStatus("PENDING");

        Long approvedBookings =
                bookingRepository.countByStatus("APPROVED");

        Long completedBookings =
                bookingRepository.countByStatus("COMPLETED");

        Long totalEquipment =
                equipmentRepository.count();

        Long availableEquipment =
                equipmentRepository.countByStatus("AVAILABLE");

        Long bookedEquipment =
                equipmentRepository.countByStatus("BOOKED");

        Long totalUsers =
                userRepository.count();

        Long activeUsers =
                userRepository.countByIsActive(true);

        return new DashboardStatsDTO(
                totalBookings,
                pendingBookings,
                approvedBookings,
                completedBookings,
                totalEquipment,
                availableEquipment,
                bookedEquipment,
                totalUsers,
                activeUsers
        );
    }


    // =========================================================
    // WEEKLY UTILIZATION
    // =========================================================

    public List<UtilizationPointDTO> getWeeklyUtilization() {

        List<Object[]> result =
                bookingRepository.getWeeklyUtilization();

        List<UtilizationPointDTO> response =
                new ArrayList<>();

        for (Object[] row : result) {

            String day =
                    row[0].toString().trim();

            Long usage =
                    ((Number) row[1]).longValue();

            response.add(
                    new UtilizationPointDTO(
                            day,
                            usage
                    )
            );
        }

        return response;
    }


    // =========================================================
// MONTHLY HEATMAP
// =========================================================

    public List<MonthlyHeatmapDTO> getMonthlyHeatmap() {

        List<Object[]> result =
                bookingRepository.getMonthlyHeatmap();

        List<MonthlyHeatmapDTO> response =
                new ArrayList<>();

        if (result == null || result.isEmpty()) {
            return response;
        }

        for (Object[] row : result) {

            if (row == null || row.length < 3) {
                continue;
            }

            if (row[0] == null ||
                    row[1] == null ||
                    row[2] == null) {
                continue;
            }

            Integer month =
                    ((Number) row[0]).intValue();

            Integer day =
                    ((Number) row[1]).intValue();

            Long bookings =
                    ((Number) row[2]).longValue();

            // Safety validation
            if (month < 1 || month > 12) {
                continue;
            }

            if (day < 1 || day > 31) {
                continue;
            }

            response.add(
                    new MonthlyHeatmapDTO(
                            month,
                            day,
                            bookings
                    )
            );
        }

        return response;
    }

    // =========================================================
    // DEPARTMENT STATISTICS
    // =========================================================

    public List<DepartmentStatDTO> getDepartmentStatistics() {

        List<Object[]> result =
                departmentRepository.getDepartmentStatistics();

        List<DepartmentStatDTO> response =
                new ArrayList<>();

        for (Object[] row : result) {

            response.add(
                    new DepartmentStatDTO(
                            row[0].toString(),
                            ((Number) row[1]).longValue()
                    )
            );
        }

        return response;
    }


    // =========================================================
    // EQUIPMENT HEATMAP
    // =========================================================

    public List<HeatmapDTO> getEquipmentHeatmap() {

        List<Object[]> usageData =
                bookingRepository.getEquipmentUsage();

        List<HeatmapDTO> response =
                new ArrayList<>();

        Long totalBookings =
                bookingRepository.count();

        for (Object[] row : usageData) {

            Integer equipmentId =
                    ((Number) row[0]).intValue();

            Long bookings =
                    ((Number) row[1]).longValue();

            Equipment equipment =
                    equipmentRepository
                            .findById(equipmentId)
                            .orElse(null);

            String equipmentName =
                    equipment != null
                            ? equipment.getEquipmentName()
                            : "Unknown Equipment";

            Double utilization = 0.0;

            if (totalBookings > 0) {

                utilization =
                        (bookings * 100.0)
                                / totalBookings;
            }

            response.add(
                    new HeatmapDTO(
                            equipmentId,
                            equipmentName,
                            bookings,
                            Math.round(
                                    utilization * 100.0
                            ) / 100.0
                    )
            );
        }

        return response;
    }


    // =========================================================
    // TOP UTILIZED EQUIPMENT
    // =========================================================

    public List<HeatmapDTO> getTopUtilizedEquipment() {

        List<HeatmapDTO> heatmap =
                getEquipmentHeatmap();

        return heatmap.stream()
                .limit(10)
                .toList();
    }


    // =========================================================
    // DEMAND ANALYSIS
    // =========================================================

    public List<DemandAnalysisDTO> getDemandAnalysis() {

        List<Object[]> result =
                bookingRepository.getEquipmentDemand();

        List<DemandAnalysisDTO> response =
                new ArrayList<>();

        Long totalBookings =
                bookingRepository.count();

        for (Object[] row : result) {

            Integer equipmentId =
                    ((Number) row[0]).intValue();

            Long equipmentBookings =
                    ((Number) row[1]).longValue();

            Equipment equipment =
                    equipmentRepository
                            .findById(equipmentId)
                            .orElse(null);

            String equipmentName =
                    equipment != null
                            ? equipment.getEquipmentName()
                            : "Unknown Equipment";


            // -------------------------------------------------
            // STATUS COUNTS
            // -------------------------------------------------

            Long pendingBookings =
                    getEquipmentStatusCount(
                            equipmentId,
                            "PENDING"
                    );

            Long approvedBookings =
                    getEquipmentStatusCount(
                            equipmentId,
                            "APPROVED"
                    );

            Long completedBookings =
                    getEquipmentStatusCount(
                            equipmentId,
                            "COMPLETED"
                    );


            // -------------------------------------------------
            // DEMAND PERCENTAGE
            // -------------------------------------------------

            Double demandPercentage = 0.0;

            if (totalBookings != null &&
                    totalBookings > 0) {

                demandPercentage =
                        (equipmentBookings * 100.0)
                                / totalBookings;
            }

            demandPercentage =
                    Math.round(
                            demandPercentage * 100.0
                    ) / 100.0;


            // -------------------------------------------------
            // DEMAND LEVEL
            // -------------------------------------------------

            String demandLevel =
                    calculateDemandLevel(
                            demandPercentage
                    );


            response.add(
                    new DemandAnalysisDTO(
                            equipmentId,
                            equipmentName,
                            equipmentBookings,
                            pendingBookings,
                            approvedBookings,
                            completedBookings,
                            demandPercentage,
                            demandLevel
                    )
            );
        }

        return response;
    }
    // =========================================================
// DEPARTMENT DEMAND ANALYSIS
// =========================================================

    public List<DepartmentDemandDTO> getDepartmentDemandAnalysis() {

        List<Object[]> result =
                bookingRepository.getDepartmentDemand();

        List<DepartmentDemandDTO> response =
                new ArrayList<>();

        Long totalBookings =
                bookingRepository.count();

        for (Object[] row : result) {

            Integer departmentId =
                    ((Number) row[0]).intValue();

            String departmentName =
                    row[1].toString();

            Long departmentBookings =
                    ((Number) row[2]).longValue();


            // -------------------------------------------------
            // DEMAND PERCENTAGE
            // -------------------------------------------------

            Double demandPercentage = 0.0;

            if (totalBookings != null &&
                    totalBookings > 0) {

                demandPercentage =
                        (departmentBookings * 100.0)
                                / totalBookings;
            }

            demandPercentage =
                    Math.round(
                            demandPercentage * 100.0
                    ) / 100.0;


            // -------------------------------------------------
            // DEMAND LEVEL
            // -------------------------------------------------

            String demandLevel =
                    calculateDemandLevel(
                            demandPercentage
                    );


            response.add(
                    new DepartmentDemandDTO(
                            departmentId,
                            departmentName,
                            departmentBookings,
                            demandPercentage,
                            demandLevel
                    )
            );
        }

        return response;
    }



    // =========================================================
    // EQUIPMENT STATUS COUNT
    // =========================================================

    private Long getEquipmentStatusCount(
            Integer equipmentId,
            String status
    ) {

        List<com.project.Lab.Resource.Utilization.Platform.entity.Booking> bookings =
                bookingRepository.findByEquipmentId(
                        equipmentId
                );

        return bookings.stream()
                .filter(
                        booking ->
                                status.equalsIgnoreCase(
                                        booking.getStatus()
                                )
                )
                .count();
    }


    // =========================================================
    // DEMAND LEVEL
    // =========================================================

    private String calculateDemandLevel(
            Double demandPercentage
    ) {

        if (demandPercentage >= 30.0) {

            return "HIGH";
        }

        if (demandPercentage >= 10.0) {

            return "MEDIUM";
        }

        return "LOW";
    }
}