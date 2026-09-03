package com.project.Lab.Resource.Utilization.Platform.repository;

import com.project.Lab.Resource.Utilization.Platform.entity.Booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository
        extends JpaRepository<Booking, Integer> {


    // =====================================================
    // USER BOOKINGS
    // =====================================================

    List<Booking> findByUserId(
            Integer userId
    );

    List<Booking> findByUserIdOrderByStartTimeDesc(
            Integer userId
    );


    // =====================================================
    // EQUIPMENT BOOKINGS
    // =====================================================

    List<Booking> findByEquipmentId(
            Integer equipmentId
    );


    // =====================================================
    // STATUS
    // =====================================================

    List<Booking> findByStatus(
            String status
    );

    List<Booking> findByStatusIgnoreCase(
            String status
    );


    // =====================================================
    // ORDERED BOOKINGS
    // =====================================================

    List<Booking> findAllByOrderByCreatedAtDesc();


    // =====================================================
    // CONFLICT CHECK WHILE CREATING
    //
    // PENDING is included intentionally so multiple users
    // cannot request exactly the same equipment/time slot.
    // =====================================================

    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.equipmentId = :equipmentId

            AND UPPER(b.status) IN (
                'PENDING',
                'APPROVED',
                'IN_USE'
            )

            AND b.startTime < :endTime
            AND b.endTime > :startTime
            """)
    List<Booking> findConflictingBookings(

            @Param("equipmentId")
            Integer equipmentId,

            @Param("startTime")
            LocalDateTime startTime,

            @Param("endTime")
            LocalDateTime endTime
    );


    // =====================================================
    // CONFLICT CHECK DURING APPROVAL
    //
    // Excludes current booking.
    // =====================================================

    @Query("""
            SELECT b
            FROM Booking b

            WHERE b.equipmentId = :equipmentId

            AND b.bookingId <> :bookingId

            AND UPPER(b.status) IN (
                'APPROVED',
                'IN_USE'
            )

            AND b.startTime < :endTime
            AND b.endTime > :startTime
            """)
    List<Booking> findApprovedConflictsExcludingBooking(

            @Param("equipmentId")
            Integer equipmentId,

            @Param("startTime")
            LocalDateTime startTime,

            @Param("endTime")
            LocalDateTime endTime,

            @Param("bookingId")
            Integer bookingId
    );
    Long countByStatus(String status);

    // =====================================================
// DASHBOARD ANALYTICS
// =====================================================

    @Query("""
       SELECT COUNT(b)
       FROM Booking b
       WHERE FUNCTION('DATE', b.createdAt) = CURRENT_DATE
       """)
    Long getTodayBookings();

    @Query("""
       SELECT COUNT(b)
       FROM Booking b
       WHERE MONTH(b.createdAt) = MONTH(CURRENT_DATE)
       AND YEAR(b.createdAt) = YEAR(CURRENT_DATE)
       """)
    Long getCurrentMonthBookings();

    @Query(value = """
SELECT
TRIM(TO_CHAR(created_at,'Day')) AS day,
COUNT(*) AS usage
FROM bookings
GROUP BY
TRIM(TO_CHAR(created_at,'Day')),
EXTRACT(DOW FROM created_at)
ORDER BY
EXTRACT(DOW FROM created_at)
""", nativeQuery = true)
    List<Object[]> getWeeklyUtilization();

    @Query("""
       SELECT b.equipmentId,
              COUNT(b)
       FROM Booking b
       WHERE UPPER(b.status) IN ('APPROVED','COMPLETED','IN_USE')
       GROUP BY b.equipmentId
       ORDER BY COUNT(b) DESC
       """)
    List<Object[]> getEquipmentUsage();
    // =====================================================
// MONTHLY HEATMAP
// Current-year bookings grouped by month and day
// =====================================================

    @Query(value = """
    SELECT
        CAST(EXTRACT(MONTH FROM created_at) AS INTEGER) AS month,
        CAST(EXTRACT(DAY FROM created_at) AS INTEGER) AS day,
        COUNT(*) AS bookings
    FROM bookings
    WHERE EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_DATE)
      AND UPPER(status) NOT IN ('CANCELLED', 'REJECTED')
    GROUP BY
        EXTRACT(MONTH FROM created_at),
        EXTRACT(DAY FROM created_at)
    ORDER BY
        month ASC,
        day ASC
    """, nativeQuery = true)
    List<Object[]> getMonthlyHeatmap();
    // =====================================================
// DEMAND ANALYSIS - EQUIPMENT
// =====================================================

    @Query("""
       SELECT b.equipmentId,
              COUNT(b)
       FROM Booking b
       GROUP BY b.equipmentId
       ORDER BY COUNT(b) DESC
       """)
    List<Object[]> getEquipmentDemand();


// =====================================================
// DEMAND ANALYSIS - DEPARTMENT
// =====================================================

    @Query("""
       SELECT d.departmentId,
              d.departmentName,
              COUNT(b)
       FROM Booking b
       JOIN Equipment e
            ON e.equipmentId = b.equipmentId
       JOIN Department d
            ON d.departmentId = e.departmentId
       GROUP BY d.departmentId, d.departmentName
       ORDER BY COUNT(b) DESC
       """)
    List<Object[]> getDepartmentDemand();

}