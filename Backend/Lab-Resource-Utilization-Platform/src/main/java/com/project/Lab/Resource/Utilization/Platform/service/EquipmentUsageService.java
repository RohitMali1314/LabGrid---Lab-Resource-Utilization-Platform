package com.project.Lab.Resource.Utilization.Platform.service;

import com.project.Lab.Resource.Utilization.Platform.dto.DepartmentUtilizationDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.EquipmentUsageResponseDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.EquipmentUtilizationDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.PeakUsageDTO;
import com.project.Lab.Resource.Utilization.Platform.dto.UtilizationSummaryDTO;
import com.project.Lab.Resource.Utilization.Platform.entity.Department;
import com.project.Lab.Resource.Utilization.Platform.entity.Equipment;
import com.project.Lab.Resource.Utilization.Platform.entity.EquipmentUsage;
import com.project.Lab.Resource.Utilization.Platform.repository.DepartmentRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.EquipmentRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.EquipmentUsageRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EquipmentUsageService {

    private static final String IN_USE = "IN_USE";
    private static final String COMPLETED = "COMPLETED";

    /*
     * Default laboratory operating window.
     *
     * This is used only for utilization calculations.
     * It does NOT change booking timings.
     */
    private static final int DEFAULT_DAILY_AVAILABLE_HOURS = 8;


    @Autowired
    private EquipmentUsageRepository equipmentUsageRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;


    // =========================================================
    // START EQUIPMENT USAGE
    // =========================================================

    @Transactional
    public EquipmentUsage startUsage(
            Integer bookingId,
            Integer equipmentId,
            Integer userId
    ) {

        if (bookingId == null) {
            throw new RuntimeException(
                    "Booking ID is required"
            );
        }

        if (equipmentId == null) {
            throw new RuntimeException(
                    "Equipment ID is required"
            );
        }

        if (userId == null) {
            throw new RuntimeException(
                    "User ID is required"
            );
        }

        equipmentRepository.findById(equipmentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Equipment not found"
                        )
                );

        // Prevent duplicate active usage for same booking.
        if (equipmentUsageRepository
                .existsByBookingIdAndStatus(
                        bookingId,
                        IN_USE
                )) {

            throw new RuntimeException(
                    "Equipment usage is already active for this booking"
            );
        }

        // Prevent same equipment from being actively used
        // by another booking.
        if (equipmentUsageRepository
                .findFirstByEquipmentIdAndStatus(
                        equipmentId,
                        IN_USE
                )
                .isPresent()) {

            throw new RuntimeException(
                    "Equipment is already in use"
            );
        }

        EquipmentUsage usage =
                new EquipmentUsage();

        LocalDateTime now =
                LocalDateTime.now();

        usage.setBookingId(
                bookingId
        );

        usage.setEquipmentId(
                equipmentId
        );

        usage.setUserId(
                userId
        );

        usage.setStartTime(
                now
        );

        usage.setStatus(
                IN_USE
        );

        usage.setCreatedAt(
                now
        );

        usage.setUpdatedAt(
                now
        );

        return equipmentUsageRepository.save(
                usage
        );
    }


    // =========================================================
    // STOP EQUIPMENT USAGE
    // =========================================================

    @Transactional
    public EquipmentUsage stopUsage(
            Integer bookingId
    ) {

        EquipmentUsage usage =
                equipmentUsageRepository
                        .findByBookingIdAndStatus(
                                bookingId,
                                IN_USE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Active equipment usage not found for booking"
                                )
                        );

        LocalDateTime endTime =
                LocalDateTime.now();

        long minutes =
                Duration.between(
                        usage.getStartTime(),
                        endTime
                ).toMinutes();

        if (minutes < 0) {
            minutes = 0;
        }

        usage.setEndTime(
                endTime
        );

        usage.setDurationMinutes(
                minutes
        );

        usage.setStatus(
                COMPLETED
        );

        usage.setUpdatedAt(
                endTime
        );

        return equipmentUsageRepository.save(
                usage
        );
    }


    // =========================================================
    // GET USAGE BY ID
    // =========================================================

    public EquipmentUsage getUsageById(
            Integer usageId
    ) {

        return equipmentUsageRepository
                .findById(usageId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Equipment usage record not found"
                        )
                );
    }


    // =========================================================
    // GET USAGE BY BOOKING
    // =========================================================

    public List<EquipmentUsage> getByBooking(
            Integer bookingId
    ) {

        return equipmentUsageRepository
                .findByBookingId(
                        bookingId
                );
    }


    // =========================================================
    // GET USAGE BY EQUIPMENT
    // =========================================================

    public List<EquipmentUsage> getByEquipment(
            Integer equipmentId
    ) {

        return equipmentUsageRepository
                .findByEquipmentId(
                        equipmentId
                );
    }


    // =========================================================
    // GET USAGE BY USER
    // =========================================================

    public List<EquipmentUsage> getByUser(
            Integer userId
    ) {

        return equipmentUsageRepository
                .findByUserId(
                        userId
                );
    }


    // =========================================================
    // GET CURRENTLY ACTIVE USAGE
    // =========================================================

    public List<EquipmentUsage> getActiveUsage() {

        return equipmentUsageRepository
                .findCurrentlyActive();
    }


    // =========================================================
    // GET USAGE FOR PERIOD
    // =========================================================

    public List<EquipmentUsage> getUsageForPeriod(
            LocalDateTime start,
            LocalDateTime end
    ) {

        validateDateRange(
                start,
                end
        );

        return equipmentUsageRepository
                .findByStartTimeGreaterThanEqualAndStartTimeLessThan(
                        start,
                        end
                );
    }


    // =========================================================
    // OVERALL UTILIZATION SUMMARY
    // =========================================================

    public UtilizationSummaryDTO getUtilizationSummary(
            LocalDateTime start,
            LocalDateTime end
    ) {

        validateDateRange(
                start,
                end
        );

        List<Equipment> equipmentList =
                equipmentRepository.findAll();

        List<EquipmentUsage> usages =
                equipmentUsageRepository
                        .findByStartTimeGreaterThanEqualAndStartTimeLessThan(
                                start,
                                end
                        );

        long totalUsageMinutes = usages.stream()
                .filter(usage ->
                        COMPLETED.equalsIgnoreCase(
                                usage.getStatus()
                        )
                )
                .mapToLong(usage ->
                        usage.getDurationMinutes() == null
                                ? 0L
                                : usage.getDurationMinutes()
                )
                .sum();

        long completedSessions = usages.stream()
                .filter(usage ->
                        COMPLETED.equalsIgnoreCase(
                                usage.getStatus()
                        )
                )
                .count();

        long activeEquipment = usages.stream()
                .filter(usage ->
                        IN_USE.equalsIgnoreCase(
                                usage.getStatus()
                        )
                )
                .map(EquipmentUsage::getEquipmentId)
                .distinct()
                .count();

        long availableMinutes =
                calculateAvailableMinutes(
                        start,
                        end
                );

        long totalAvailableMinutes =
                availableMinutes *
                        equipmentList.size();

        BigDecimal usageHours =
                minutesToHours(
                        totalUsageMinutes
                );

        BigDecimal availableHours =
                minutesToHours(
                        totalAvailableMinutes
                );

        BigDecimal utilization =
                calculatePercentage(
                        totalUsageMinutes,
                        totalAvailableMinutes
                );

        BigDecimal idleHours =
                minutesToHours(
                        Math.max(
                                0L,
                                totalAvailableMinutes
                                        - totalUsageMinutes
                        )
                );

        UtilizationSummaryDTO dto =
                new UtilizationSummaryDTO();

        dto.setTotalEquipment(
                (long) equipmentList.size()
        );

        dto.setTotalUsageSessions(
                (long) usages.size()
        );

        dto.setActiveEquipment(
                activeEquipment
        );

        dto.setCompletedSessions(
                completedSessions
        );

        dto.setTotalUsageHours(
                usageHours
        );

        dto.setTotalAvailableHours(
                availableHours
        );

        dto.setUtilizationPercentage(
                utilization
        );

        dto.setIdleHours(
                idleHours
        );

        return dto;
    }


    // =========================================================
    // EQUIPMENT-WISE UTILIZATION
    // =========================================================

    public List<EquipmentUtilizationDTO>
    getEquipmentUtilization(
            LocalDateTime start,
            LocalDateTime end
    ) {

        validateDateRange(
                start,
                end
        );

        List<Equipment> equipmentList =
                equipmentRepository.findAll();

        List<EquipmentUtilizationDTO> result =
                new ArrayList<>();

        long availableMinutes =
                calculateAvailableMinutes(
                        start,
                        end
                );

        for (Equipment equipment :
                equipmentList) {

            List<EquipmentUsage> usages =
                    equipmentUsageRepository
                            .findByEquipmentIdAndStartTimeGreaterThanEqualAndStartTimeLessThan(
                                    equipment.getEquipmentId(),
                                    start,
                                    end
                            );

            long usageMinutes =
                    usages.stream()
                            .filter(usage ->
                                    COMPLETED.equalsIgnoreCase(
                                            usage.getStatus()
                                    )
                            )
                            .mapToLong(usage ->
                                    usage.getDurationMinutes() == null
                                            ? 0L
                                            : usage.getDurationMinutes()
                            )
                            .sum();

            long sessions =
                    usages.stream()
                            .filter(usage ->
                                    COMPLETED.equalsIgnoreCase(
                                            usage.getStatus()
                                    )
                            )
                            .count();

            BigDecimal utilization =
                    calculatePercentage(
                            usageMinutes,
                            availableMinutes
                    );

            BigDecimal usageHours =
                    minutesToHours(
                            usageMinutes
                    );

            BigDecimal availableHours =
                    minutesToHours(
                            availableMinutes
                    );

            BigDecimal idleHours =
                    minutesToHours(
                            Math.max(
                                    0L,
                                    availableMinutes
                                            - usageMinutes
                            )
                    );

            EquipmentUtilizationDTO dto =
                    new EquipmentUtilizationDTO();

            dto.setEquipmentId(
                    equipment.getEquipmentId()
            );

            dto.setEquipmentName(
                    equipment.getEquipmentName()
            );

            dto.setModelNo(
                    equipment.getModelNo()
            );

            dto.setStatus(
                    equipment.getStatus()
            );

            dto.setUsageSessions(
                    sessions
            );

            dto.setUsageMinutes(
                    usageMinutes
            );

            dto.setUsageHours(
                    usageHours
            );

            dto.setAvailableHours(
                    availableHours
            );

            dto.setIdleHours(
                    idleHours
            );

            dto.setUtilizationPercentage(
                    utilization
            );

            result.add(dto);
        }

        result.sort(
                Comparator.comparing(
                        EquipmentUtilizationDTO::getUtilizationPercentage,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );

        return result;
    }


    // =========================================================
    // DEPARTMENT-WISE UTILIZATION
    // =========================================================

    public List<DepartmentUtilizationDTO>
    getDepartmentUtilization(
            LocalDateTime start,
            LocalDateTime end
    ) {

        validateDateRange(
                start,
                end
        );

        List<Equipment> equipmentList =
                equipmentRepository.findAll();

        Map<Integer, List<Equipment>> grouped =
                new HashMap<>();

        for (Equipment equipment :
                equipmentList) {

            if (equipment.getDepartmentId() == null) {
                continue;
            }

            grouped.computeIfAbsent(
                    equipment.getDepartmentId(),
                    key -> new ArrayList<>()
            ).add(equipment);
        }

        List<DepartmentUtilizationDTO> result =
                new ArrayList<>();

        long availableMinutesPerEquipment =
                calculateAvailableMinutes(
                        start,
                        end
                );

        for (Map.Entry<Integer, List<Equipment>> entry :
                grouped.entrySet()) {

            Integer departmentId =
                    entry.getKey();

            List<Equipment> departmentEquipment =
                    entry.getValue();

            long totalUsageMinutes = 0L;
            long usageSessions = 0L;

            for (Equipment equipment :
                    departmentEquipment) {

                List<EquipmentUsage> usages =
                        equipmentUsageRepository
                                .findByEquipmentIdAndStartTimeGreaterThanEqualAndStartTimeLessThan(
                                        equipment.getEquipmentId(),
                                        start,
                                        end
                                );

                for (EquipmentUsage usage :
                        usages) {

                    if (COMPLETED.equalsIgnoreCase(
                            usage.getStatus()
                    )) {

                        usageSessions++;

                        if (usage.getDurationMinutes() != null) {

                            totalUsageMinutes +=
                                    usage.getDurationMinutes();
                        }
                    }
                }
            }

            long availableMinutes =
                    availableMinutesPerEquipment
                            * departmentEquipment.size();

            BigDecimal usageHours =
                    minutesToHours(
                            totalUsageMinutes
                    );

            BigDecimal availableHours =
                    minutesToHours(
                            availableMinutes
                    );

            BigDecimal idleHours =
                    minutesToHours(
                            Math.max(
                                    0L,
                                    availableMinutes
                                            - totalUsageMinutes
                            )
                    );

            BigDecimal utilization =
                    calculatePercentage(
                            totalUsageMinutes,
                            availableMinutes
                    );

            String departmentName =
                    departmentRepository
                            .findById(departmentId)
                            .map(Department::getDepartmentName)
                            .orElse(
                                    "Department " + departmentId
                            );

            DepartmentUtilizationDTO dto =
                    new DepartmentUtilizationDTO();

            dto.setDepartmentId(
                    departmentId
            );

            dto.setDepartmentName(
                    departmentName
            );

            dto.setTotalEquipment(
                    (long) departmentEquipment.size()
            );

            dto.setUsageSessions(
                    usageSessions
            );

            dto.setUsageMinutes(
                    totalUsageMinutes
            );

            dto.setUsageHours(
                    usageHours
            );

            dto.setAvailableHours(
                    availableHours
            );

            dto.setIdleHours(
                    idleHours
            );

            dto.setUtilizationPercentage(
                    utilization
            );

            result.add(dto);
        }

        result.sort(
                Comparator.comparing(
                        DepartmentUtilizationDTO::getUtilizationPercentage,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );

        return result;
    }


    // =========================================================
    // PEAK USAGE BY DAY
    // =========================================================

    public List<PeakUsageDTO> getPeakUsage(
            LocalDateTime start,
            LocalDateTime end
    ) {

        validateDateRange(
                start,
                end
        );

        List<EquipmentUsage> usages =
                equipmentUsageRepository
                        .findByStartTimeGreaterThanEqualAndStartTimeLessThan(
                                start,
                                end
                        );

        Map<String, Long> sessionMap =
                new HashMap<>();

        Map<String, Long> minuteMap =
                new HashMap<>();

        for (EquipmentUsage usage :
                usages) {

            if (!COMPLETED.equalsIgnoreCase(
                    usage.getStatus()
            )) {
                continue;
            }

            String period =
                    usage.getStartTime()
                            .toLocalDate()
                            .toString();

            sessionMap.put(
                    period,
                    sessionMap.getOrDefault(
                            period,
                            0L
                    ) + 1
            );

            minuteMap.put(
                    period,
                    minuteMap.getOrDefault(
                            period,
                            0L
                    ) + (
                            usage.getDurationMinutes() == null
                                    ? 0L
                                    : usage.getDurationMinutes()
                    )
            );
        }

        List<PeakUsageDTO> result =
                new ArrayList<>();

        for (String period :
                sessionMap.keySet()) {

            Long sessions =
                    sessionMap.get(period);

            Long minutes =
                    minuteMap.getOrDefault(
                            period,
                            0L
                    );

            result.add(
                    new PeakUsageDTO(
                            period,
                            sessions,
                            minutes,
                            minutesToHours(
                                    minutes
                            )
                    )
            );
        }

        result.sort(
                Comparator.comparing(
                        PeakUsageDTO::getUsageMinutes,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );

        return result;
    }


    // =========================================================
    // IDLE HOURS FOR EQUIPMENT
    // =========================================================

    public BigDecimal getIdleHours(
            Integer equipmentId,
            LocalDateTime start,
            LocalDateTime end
    ) {

        validateDateRange(
                start,
                end
        );

        equipmentRepository.findById(
                equipmentId
        ).orElseThrow(() ->
                new RuntimeException(
                        "Equipment not found"
                )
        );

        long availableMinutes =
                calculateAvailableMinutes(
                        start,
                        end
                );

        Long usageMinutes =
                equipmentUsageRepository
                        .getTotalUsageMinutes(
                                equipmentId,
                                start,
                                end
                        );

        if (usageMinutes == null) {
            usageMinutes = 0L;
        }

        return minutesToHours(
                Math.max(
                        0L,
                        availableMinutes
                                - usageMinutes
                )
        );
    }


    // =========================================================
    // UTILIZATION PERCENTAGE FOR EQUIPMENT
    // =========================================================

    public BigDecimal getEquipmentUtilizationPercentage(
            Integer equipmentId,
            LocalDateTime start,
            LocalDateTime end
    ) {

        validateDateRange(
                start,
                end
        );

        equipmentRepository.findById(
                equipmentId
        ).orElseThrow(() ->
                new RuntimeException(
                        "Equipment not found"
                )
        );

        long availableMinutes =
                calculateAvailableMinutes(
                        start,
                        end
                );

        Long usageMinutes =
                equipmentUsageRepository
                        .getTotalUsageMinutes(
                                equipmentId,
                                start,
                                end
                        );

        if (usageMinutes == null) {
            usageMinutes = 0L;
        }

        return calculatePercentage(
                usageMinutes,
                availableMinutes
        );
    }


    // =========================================================
    // DTO MAPPER
    // =========================================================

    public EquipmentUsageResponseDTO
    toResponseDTO(
            EquipmentUsage usage
    ) {

        EquipmentUsageResponseDTO dto =
                new EquipmentUsageResponseDTO();

        dto.setUsageId(
                usage.getUsageId()
        );

        dto.setBookingId(
                usage.getBookingId()
        );

        dto.setEquipmentId(
                usage.getEquipmentId()
        );

        dto.setUserId(
                usage.getUserId()
        );

        dto.setStartTime(
                usage.getStartTime()
        );

        dto.setEndTime(
                usage.getEndTime()
        );

        dto.setDurationMinutes(
                usage.getDurationMinutes()
        );

        dto.setStatus(
                usage.getStatus()
        );

        return dto;
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private void validateDateRange(
            LocalDateTime start,
            LocalDateTime end
    ) {

        if (start == null || end == null) {

            throw new RuntimeException(
                    "Start date and end date are required"
            );
        }

        if (!end.isAfter(start)) {

            throw new RuntimeException(
                    "End date must be after start date"
            );
        }
    }


    private long calculateAvailableMinutes(
            LocalDateTime start,
            LocalDateTime end
    ) {

        long totalDays =
                ChronoUnit.DAYS.between(
                        start.toLocalDate(),
                        end.toLocalDate()
                );

        /*
         * If the period is within the same day,
         * use the actual laboratory operating window.
         */
        if (totalDays == 0) {

            return calculateDailyAvailableMinutes(
                    start.toLocalDate()
            );
        }

        long availableMinutes =
                0L;

        for (long i = 0; i <= totalDays; i++) {

            availableMinutes +=
                    calculateDailyAvailableMinutes(
                            start.toLocalDate()
                                    .plusDays(i)
                    );
        }

        return availableMinutes;
    }


    private long calculateDailyAvailableMinutes(
            java.time.LocalDate date
    ) {

        LocalTime openingTime =
                LocalTime.of(9, 0);

        LocalTime closingTime =
                openingTime.plusHours(
                        DEFAULT_DAILY_AVAILABLE_HOURS
                );

        return Duration.between(
                openingTime,
                closingTime
        ).toMinutes();
    }


    private BigDecimal minutesToHours(
            long minutes
    ) {

        return BigDecimal.valueOf(minutes)
                .divide(
                        BigDecimal.valueOf(60),
                        2,
                        RoundingMode.HALF_UP
                );
    }


    private BigDecimal calculatePercentage(
            long usedMinutes,
            long availableMinutes
    ) {

        if (availableMinutes <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal percentage =
                BigDecimal.valueOf(
                                usedMinutes
                        )
                        .multiply(
                                BigDecimal.valueOf(100)
                        )
                        .divide(
                                BigDecimal.valueOf(
                                        availableMinutes
                                ),
                                2,
                                RoundingMode.HALF_UP
                        );

        /*
         * Utilization should never be displayed
         * above 100%.
         */
        if (percentage.compareTo(
                BigDecimal.valueOf(100)
        ) > 0) {

            return BigDecimal.valueOf(100)
                    .setScale(
                            2,
                            RoundingMode.HALF_UP
                    );
        }

        return percentage;
    }
}