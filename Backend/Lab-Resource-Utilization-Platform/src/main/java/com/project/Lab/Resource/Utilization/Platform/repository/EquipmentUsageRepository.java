package com.project.Lab.Resource.Utilization.Platform.repository;

import com.project.Lab.Resource.Utilization.Platform.entity.EquipmentUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EquipmentUsageRepository
        extends JpaRepository<EquipmentUsage, Integer> {

    // =========================================================
    // BASIC LOOKUPS
    // =========================================================

    List<EquipmentUsage> findByEquipmentId(
            Integer equipmentId
    );

    List<EquipmentUsage> findByBookingId(
            Integer bookingId
    );

    List<EquipmentUsage> findByUserId(
            Integer userId
    );

    List<EquipmentUsage> findByStatus(
            String status
    );

    Optional<EquipmentUsage> findByBookingIdAndStatus(
            Integer bookingId,
            String status
    );


    // =========================================================
    // ACTIVE USAGE
    // =========================================================

    Optional<EquipmentUsage> findFirstByEquipmentIdAndStatus(
            Integer equipmentId,
            String status
    );

    List<EquipmentUsage> findByStatusOrderByStartTimeDesc(
            String status
    );


    // =========================================================
    // DATE RANGE
    // =========================================================

    List<EquipmentUsage>
    findByStartTimeGreaterThanEqualAndStartTimeLessThan(
            LocalDateTime start,
            LocalDateTime end
    );


    // =========================================================
    // EQUIPMENT + DATE RANGE
    // =========================================================

    List<EquipmentUsage>
    findByEquipmentIdAndStartTimeGreaterThanEqualAndStartTimeLessThan(
            Integer equipmentId,
            LocalDateTime start,
            LocalDateTime end
    );


    // =========================================================
    // COMPLETED USAGE
    // =========================================================

    @Query("""
        SELECT COALESCE(SUM(e.durationMinutes), 0)
        FROM EquipmentUsage e
        WHERE e.equipmentId = :equipmentId
          AND e.status = 'COMPLETED'
          AND e.startTime >= :start
          AND e.startTime < :end
    """)
    Long getTotalUsageMinutes(
            @Param("equipmentId") Integer equipmentId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    // =========================================================
    // TOTAL USAGE FOR ALL EQUIPMENT
    // =========================================================

    @Query("""
        SELECT COALESCE(SUM(e.durationMinutes), 0)
        FROM EquipmentUsage e
        WHERE e.status = 'COMPLETED'
          AND e.startTime >= :start
          AND e.startTime < :end
    """)
    Long getTotalUsageMinutesForPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    // =========================================================
    // USAGE COUNT
    // =========================================================

    @Query("""
        SELECT COUNT(e)
        FROM EquipmentUsage e
        WHERE e.equipmentId = :equipmentId
          AND e.status = 'COMPLETED'
          AND e.startTime >= :start
          AND e.startTime < :end
    """)
    Long countCompletedUsage(
            @Param("equipmentId") Integer equipmentId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    // =========================================================
    // ACTIVE EQUIPMENT COUNT
    // =========================================================

    @Query("""
        SELECT COUNT(e)
        FROM EquipmentUsage e
        WHERE e.status = 'IN_USE'
    """)
    Long countCurrentlyInUse();


    // =========================================================
    // ALL ACTIVE USAGE RECORDS
    // =========================================================

    @Query("""
        SELECT e
        FROM EquipmentUsage e
        WHERE e.status = 'IN_USE'
        ORDER BY e.startTime ASC
    """)
    List<EquipmentUsage> findCurrentlyActive();


    // =========================================================
    // PREVENT DUPLICATE ACTIVE USAGE
    // =========================================================

    boolean existsByBookingIdAndStatus(
            Integer bookingId,
            String status
    );


    // =========================================================
    // LATEST USAGE RECORD
    // =========================================================

    Optional<EquipmentUsage>
    findFirstByEquipmentIdOrderByStartTimeDesc(
            Integer equipmentId
    );
}