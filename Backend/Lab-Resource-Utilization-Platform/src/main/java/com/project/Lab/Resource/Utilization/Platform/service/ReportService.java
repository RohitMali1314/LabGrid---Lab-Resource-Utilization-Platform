package com.project.Lab.Resource.Utilization.Platform.service;

import com.project.Lab.Resource.Utilization.Platform.dto.CostAnalysisDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.ReportResponseDTO;
import com.project.Lab.Resource.Utilization.Platform.entity.Booking;
import com.project.Lab.Resource.Utilization.Platform.entity.CostAllocation;
import com.project.Lab.Resource.Utilization.Platform.entity.Equipment;
import com.project.Lab.Resource.Utilization.Platform.entity.Maintenance;
import com.project.Lab.Resource.Utilization.Platform.repository.BookingRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.CostAllocationRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.EquipmentRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.MaintenanceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private CostAllocationRepository costAllocationRepository;


    // =========================================================
    // DASHBOARD REPORT
    // =========================================================

    public ReportResponseDTO getDashboardReport() {

        ReportResponseDTO report =
                new ReportResponseDTO();

        // Equipment

        report.setTotalEquipment(
                equipmentRepository.count()
        );

        report.setAvailableEquipment(
                equipmentRepository.countByStatus("AVAILABLE")
        );

        report.setBookedEquipment(
                equipmentRepository.countByStatus("BOOKED")
        );

        report.setUnderMaintenanceEquipment(
                equipmentRepository.countByStatus(
                        "UNDER_MAINTENANCE"
                )
        );


        // Booking

        report.setTotalBookings(
                bookingRepository.count()
        );

        report.setPendingBookings(
                bookingRepository.countByStatus("PENDING")
        );

        report.setApprovedBookings(
                bookingRepository.countByStatus("APPROVED")
        );

        report.setCompletedBookings(
                bookingRepository.countByStatus("COMPLETED")
        );


        // Maintenance

        report.setTotalMaintenance(
                maintenanceRepository.count()
        );

        report.setPendingMaintenance(
                maintenanceRepository.countByStatus(
                        "PENDING"
                )
        );

        report.setInProgressMaintenance(
                maintenanceRepository.countByStatus(
                        "IN_PROGRESS"
                )
        );

        report.setCompletedMaintenance(
                maintenanceRepository.countByStatus(
                        "COMPLETED"
                )
        );

        return report;
    }


    // =========================================================
    // EQUIPMENT REPORT
    // =========================================================

    public List<Equipment> getEquipmentReport() {

        return equipmentRepository.findAll();
    }


    // =========================================================
    // BOOKING REPORT
    // =========================================================

    public List<Booking> getBookingReport() {

        return bookingRepository
                .findAllByOrderByCreatedAtDesc();
    }


    // =========================================================
    // MAINTENANCE REPORT
    // =========================================================

    public List<Maintenance> getMaintenanceReport() {

        return maintenanceRepository.findAll();
    }


    // =========================================================
    // TODAY BOOKINGS
    // =========================================================

    public Long getTodayBookings() {

        return bookingRepository.getTodayBookings();
    }


    // =========================================================
    // CURRENT MONTH BOOKINGS
    // =========================================================

    public Long getCurrentMonthBookings() {

        return bookingRepository
                .getCurrentMonthBookings();
    }


    // =========================================================
    // WEEKLY UTILIZATION
    // =========================================================

    public List<Object[]> getWeeklyUtilization() {

        return bookingRepository
                .getWeeklyUtilization();
    }


    // =========================================================
    // EQUIPMENT USAGE REPORT
    // =========================================================

    public List<Object[]> getEquipmentUsageReport() {

        return bookingRepository
                .getEquipmentUsage();
    }


    // =========================================================
    // EQUIPMENT STATUS COUNTS
    // =========================================================

    public Long getAvailableEquipmentCount() {

        return equipmentRepository
                .countByStatus("AVAILABLE");
    }


    public Long getBookedEquipmentCount() {

        return equipmentRepository
                .countByStatus("BOOKED");
    }


    public Long getMaintenanceEquipmentCount() {

        return equipmentRepository
                .countByStatus(
                        "UNDER_MAINTENANCE"
                );
    }


    // =========================================================
    // BOOKING STATUS COUNTS
    // =========================================================

    public Long getPendingBookingsCount() {

        return bookingRepository
                .countByStatus("PENDING");
    }


    public Long getApprovedBookingsCount() {

        return bookingRepository
                .countByStatus("APPROVED");
    }


    public Long getCompletedBookingsCount() {

        return bookingRepository
                .countByStatus("COMPLETED");
    }


    // =========================================================
    // MAINTENANCE STATUS COUNTS
    // =========================================================

    public Long getPendingMaintenanceCount() {

        return maintenanceRepository
                .countByStatus("PENDING");
    }


    public Long getInProgressMaintenanceCount() {

        return maintenanceRepository
                .countByStatus("IN_PROGRESS");
    }


    public Long getCompletedMaintenanceCount() {

        return maintenanceRepository
                .countByStatus("COMPLETED");
    }


    // =========================================================
    // COST ANALYSIS
    // =========================================================

    public List<CostAnalysisDTO> getCostAnalysis() {

        List<CostAllocation> allocations =
                costAllocationRepository.findAll();

        List<CostAnalysisDTO> response =
                new ArrayList<>();


        // -----------------------------------------------------
        // FIND UNIQUE INSTITUTIONS
        // -----------------------------------------------------

        List<Integer> institutionIds =
                allocations.stream()
                        .map(
                                CostAllocation::getInstitutionId
                        )
                        .distinct()
                        .toList();


        // -----------------------------------------------------
        // CALCULATE COST FOR EACH INSTITUTION
        // -----------------------------------------------------

        for (Integer institutionId : institutionIds) {

            List<CostAllocation> institutionAllocations =
                    allocations.stream()
                            .filter(
                                    allocation ->
                                            institutionId.equals(
                                                    allocation.getInstitutionId()
                                            )
                            )
                            .toList();


            Long totalAllocations =
                    (long) institutionAllocations.size();


            BigDecimal totalAllocatedCost =
                    BigDecimal.ZERO;

            BigDecimal paidCost =
                    BigDecimal.ZERO;

            BigDecimal pendingCost =
                    BigDecimal.ZERO;

            BigDecimal approvedCost =
                    BigDecimal.ZERO;

            BigDecimal cancelledCost =
                    BigDecimal.ZERO;


            // -------------------------------------------------
            // CALCULATE AMOUNTS
            // -------------------------------------------------

            for (
                    CostAllocation allocation :
                    institutionAllocations
            ) {

                BigDecimal amount =
                        allocation.getAllocatedAmount();


                if (amount == null) {

                    amount =
                            BigDecimal.ZERO;
                }


                totalAllocatedCost =
                        totalAllocatedCost.add(
                                amount
                        );


                String status =
                        allocation.getStatus();


                if (status == null) {

                    continue;
                }


                switch (
                        status.toUpperCase()
                ) {

                    case "PAID":

                        paidCost =
                                paidCost.add(
                                        amount
                                );

                        break;


                    case "PENDING":

                        pendingCost =
                                pendingCost.add(
                                        amount
                                );

                        break;


                    case "APPROVED":

                        approvedCost =
                                approvedCost.add(
                                        amount
                                );

                        break;


                    case "CANCELLED":

                        cancelledCost =
                                cancelledCost.add(
                                        amount
                                );

                        break;


                    default:

                        break;
                }
            }


            // -------------------------------------------------
            // ROUND VALUES
            // -------------------------------------------------

            totalAllocatedCost =
                    totalAllocatedCost.setScale(
                            2,
                            java.math.RoundingMode.HALF_UP
                    );

            paidCost =
                    paidCost.setScale(
                            2,
                            java.math.RoundingMode.HALF_UP
                    );

            pendingCost =
                    pendingCost.setScale(
                            2,
                            java.math.RoundingMode.HALF_UP
                    );

            approvedCost =
                    approvedCost.setScale(
                            2,
                            java.math.RoundingMode.HALF_UP
                    );

            cancelledCost =
                    cancelledCost.setScale(
                            2,
                            java.math.RoundingMode.HALF_UP
                    );


            response.add(
                    new CostAnalysisDTO(
                            institutionId,
                            totalAllocations,
                            totalAllocatedCost,
                            paidCost,
                            pendingCost,
                            approvedCost,
                            cancelledCost
                    )
            );
        }


        return response;
    }
}