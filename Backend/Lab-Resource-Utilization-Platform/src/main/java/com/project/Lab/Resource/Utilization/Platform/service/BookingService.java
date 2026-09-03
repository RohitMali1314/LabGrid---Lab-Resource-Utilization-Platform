package com.project.Lab.Resource.Utilization.Platform.service;

import com.project.Lab.Resource.Utilization.Platform.dto.AuditLogRequestDTO;
import com.project.Lab.Resource.Utilization.Platform.entity.Booking;
import com.project.Lab.Resource.Utilization.Platform.entity.Department;
import com.project.Lab.Resource.Utilization.Platform.entity.Equipment;
import com.project.Lab.Resource.Utilization.Platform.entity.Notification;
import com.project.Lab.Resource.Utilization.Platform.entity.User;
import com.project.Lab.Resource.Utilization.Platform.repository.BookingRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.DepartmentRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.EquipmentRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.NotificationRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.ResourceShareRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BillingService billingService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private WaitlistService waitlistService;

    @Autowired
    private NotificationWebSocketService notificationWebSocketService;

    @Autowired
    private ResourceShareRepository resourceShareRepository;

    @Autowired
    private EquipmentUsageService equipmentUsageService;

    // =========================================================
    // NEW - REQUIRED FOR EXTERNAL RESOURCE SHARING
    // =========================================================

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;


    // =========================================================
    // CREATE BOOKING
    // =========================================================

    public Booking createBooking(
            Booking booking
    ) {

        // -----------------------------------------------------
        // BASIC DATE VALIDATION
        // -----------------------------------------------------

        if (
                booking.getStartTime() == null ||
                        booking.getEndTime() == null
        ) {

            throw new RuntimeException(
                    "Start time and end time are required"
            );
        }

        if (
                booking.getStartTime()
                        .isAfter(
                                booking.getEndTime()
                        )
                        ||
                        booking.getStartTime()
                                .isEqual(
                                        booking.getEndTime()
                                )
        ) {

            throw new RuntimeException(
                    "End time must be after start time"
            );
        }


        // -----------------------------------------------------
        // VALIDATE EQUIPMENT + USER + INSTITUTION ACCESS
        // -----------------------------------------------------

        validateEquipmentBookingAccess(
                booking
        );


        // -----------------------------------------------------
        // CHECK CONFLICTING BOOKINGS
        // -----------------------------------------------------

        List<Booking> conflicts =
                bookingRepository
                        .findConflictingBookings(
                                booking.getEquipmentId(),
                                booking.getStartTime(),
                                booking.getEndTime()
                        );

        if (!conflicts.isEmpty()) {

            throw new RuntimeException(
                    "Equipment already booked for selected time slot"
            );
        }


        // -----------------------------------------------------
        // CREATE BOOKING
        // -----------------------------------------------------

        booking.setCreatedAt(
                LocalDateTime.now()
        );

        booking.setStatus(
                "PENDING"
        );

        Booking saved =
                bookingRepository.save(
                        booking
                );


        // -----------------------------------------------------
        // NOTIFICATION
        // -----------------------------------------------------

        Notification notification =
                new Notification();

        notification.setUserId(
                saved.getUserId()
        );

        notification.setTitle(
                "Booking Created"
        );

        notification.setMessage(
                "Your booking request has been created successfully."
        );

        notification.setNotificationType(
                "BOOKING"
        );

        notification.setReferenceId(
                saved.getBookingId()
        );

        notification.setIsRead(false);

        notificationRepository.save(
                notification
        );


        // -----------------------------------------------------
        // EMAIL
        // -----------------------------------------------------

        User user =
                userRepository
                        .findById(
                                saved.getUserId()
                        )
                        .orElse(null);

        if (user != null) {

            emailService.sendEmail(
                    user.getEmail(),
                    "Booking Created",
                    "Your booking request has been created successfully."
            );
        }


        // -----------------------------------------------------
        // AUDIT LOG
        // -----------------------------------------------------

        AuditLogRequestDTO audit =
                new AuditLogRequestDTO();

        audit.setUserId(
                saved.getUserId()
        );

        audit.setAction(
                "CREATE"
        );

        audit.setModule(
                "BOOKING"
        );

        audit.setDescription(
                "Booking Created"
        );

        audit.setIpAddress(
                "SYSTEM"
        );

        auditLogService.saveAuditLog(
                audit
        );


        return saved;
    }


    // =========================================================
    // EXTERNAL RESOURCE SHARING ACCESS VALIDATION
    // =========================================================

    private void validateEquipmentBookingAccess(
            Booking booking
    ) {

        // -----------------------------------------------------
        // GET USER
        // -----------------------------------------------------

        User user =
                userRepository
                        .findById(
                                booking.getUserId()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found"
                                )
                        );


        // -----------------------------------------------------
        // GET EQUIPMENT
        // -----------------------------------------------------

        Equipment equipment =
                equipmentRepository
                        .findById(
                                booking.getEquipmentId()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Equipment not found"
                                )
                        );


        // -----------------------------------------------------
        // GET EQUIPMENT DEPARTMENT
        // -----------------------------------------------------

        if (equipment.getDepartmentId() == null) {

            throw new RuntimeException(
                    "Equipment is not associated with a department"
            );
        }

        Department department =
                departmentRepository
                        .findById(
                                equipment.getDepartmentId()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Equipment department not found"
                                )
                        );


        // -----------------------------------------------------
        // GET INSTITUTIONS
        // -----------------------------------------------------

        Integer userInstitutionId =
                user.getInstitutionId();

        Integer equipmentInstitutionId =
                department.getInstitutionId();


        if (userInstitutionId == null) {

            throw new RuntimeException(
                    "User is not associated with an institution"
            );
        }

        if (equipmentInstitutionId == null) {

            throw new RuntimeException(
                    "Equipment department is not associated with an institution"
            );
        }


        // -----------------------------------------------------
        // SAME INSTITUTION
        // -----------------------------------------------------
        //
        // User can book equipment belonging to
        // their own institution normally.
        //
        // -----------------------------------------------------

        if (
                userInstitutionId.equals(
                        equipmentInstitutionId
                )
        ) {

            return;
        }


        // -----------------------------------------------------
        // DIFFERENT INSTITUTION
        // -----------------------------------------------------
        //
        // External booking is allowed only when
        // an ACTIVE ResourceShare exists for:
        //
        // 1. Equipment
        // 2. Target institution
        // 3. ACTIVE status
        // 4. Share starts before/equal to booking start
        // 5. Share ends after/equal to booking end
        //
        // -----------------------------------------------------

        LocalDate bookingStartDate =
                booking.getStartTime()
                        .toLocalDate();

        LocalDate bookingEndDate =
                booking.getEndTime()
                        .toLocalDate();


        List<?> activeShares =
                resourceShareRepository
                        .findByEquipmentIdAndTargetInstitutionIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                                booking.getEquipmentId(),
                                userInstitutionId,
                                "ACTIVE",
                                bookingStartDate,
                                bookingEndDate
                        );


        if (
                activeShares == null ||
                        activeShares.isEmpty()
        ) {

            throw new RuntimeException(
                    "This equipment is not available for booking by your institution. " +
                            "An active resource sharing agreement is required."
            );
        }
    }


    // =========================================================
    // GET ALL BOOKINGS
    // =========================================================

    public List<Booking> getAllBookings() {

        return bookingRepository
                .findAll();
    }


    // =========================================================
    // GET BOOKING BY ID
    // =========================================================

    public Booking getBookingById(
            Integer id
    ) {

        return bookingRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new RuntimeException(
                                        "Booking not found"
                                )
                );
    }


    // =========================================================
    // GET BY STATUS
    // =========================================================

    public List<Booking> getBookingsByStatus(
            String status
    ) {

        return bookingRepository
                .findByStatus(status);
    }


    // =========================================================
    // APPROVE BOOKING
    // =========================================================

    public Booking approveBooking(
            Integer id
    ) {

        Booking booking =
                getBookingById(id);

        if (!"PENDING".equals(
                booking.getStatus()
        )) {

            throw new RuntimeException(
                    "Only pending bookings can be approved"
            );
        }

        booking.setStatus(
                "APPROVED"
        );

        Booking updated =
                bookingRepository.save(
                        booking
                );

        Notification notification =
                new Notification();

        notification.setUserId(
                updated.getUserId()
        );

        notification.setTitle(
                "Booking Approved"
        );

        notification.setMessage(
                "Your booking has been approved."
        );

        notification.setNotificationType(
                "BOOKING"
        );

        notification.setReferenceId(
                updated.getBookingId()
        );

        notification.setIsRead(false);

        notificationRepository.save(
                notification
        );

        User user =
                userRepository
                        .findById(
                                updated.getUserId()
                        )
                        .orElse(null);

        if (user != null) {

            emailService.sendEmail(
                    user.getEmail(),
                    "Booking Approved",
                    "Your booking has been approved."
            );

            notificationWebSocketService.sendNotification(
                    updated.getUserId(),
                    "Booking Approved",
                    "Your booking has been approved.",
                    "BOOKING"
            );
        }

        AuditLogRequestDTO audit =
                new AuditLogRequestDTO();

        audit.setUserId(
                updated.getUserId()
        );

        audit.setAction(
                "APPROVE"
        );

        audit.setModule(
                "BOOKING"
        );

        audit.setDescription(
                "Booking Approved"
        );

        audit.setIpAddress(
                "SYSTEM"
        );

        auditLogService.saveAuditLog(
                audit
        );

        return updated;
    }


    // =========================================================
    // REJECT BOOKING
    // =========================================================

    public Booking rejectBooking(
            Integer id
    ) {

        Booking booking =
                getBookingById(id);

        if (!"PENDING".equals(
                booking.getStatus()
        )) {

            throw new RuntimeException(
                    "Only pending bookings can be rejected"
            );
        }

        booking.setStatus(
                "REJECTED"
        );

        Booking updated =
                bookingRepository.save(
                        booking
                );

        Notification notification =
                new Notification();

        notification.setUserId(
                updated.getUserId()
        );

        notification.setTitle(
                "Booking Rejected"
        );

        notification.setMessage(
                "Your booking request has been rejected."
        );

        notification.setNotificationType(
                "BOOKING"
        );

        notification.setReferenceId(
                updated.getBookingId()
        );

        notification.setIsRead(false);

        notificationRepository.save(
                notification
        );

        User user =
                userRepository
                        .findById(
                                updated.getUserId()
                        )
                        .orElse(null);

        if (user != null) {

            emailService.sendEmail(
                    user.getEmail(),
                    "Booking Rejected",
                    "Your booking request has been rejected."
            );

            notificationWebSocketService.sendNotification(
                    updated.getUserId(),
                    "Booking Rejected",
                    "Your booking has been rejected.",
                    "BOOKING"
            );
        }

        AuditLogRequestDTO audit =
                new AuditLogRequestDTO();

        audit.setUserId(
                updated.getUserId()
        );

        audit.setAction(
                "REJECT"
        );

        audit.setModule(
                "BOOKING"
        );

        audit.setDescription(
                "Booking Rejected"
        );

        audit.setIpAddress(
                "SYSTEM"
        );

        auditLogService.saveAuditLog(
                audit
        );

        return updated;
    }


    // =========================================================
    // CANCEL BOOKING
    // =========================================================

    public Booking cancelBooking(
            Integer id
    ) {

        Booking booking =
                getBookingById(id);

        if (
                !"PENDING".equals(
                        booking.getStatus()
                )
                        &&
                        !"APPROVED".equals(
                                booking.getStatus()
                        )
        ) {

            throw new RuntimeException(
                    "Only pending or approved bookings can be cancelled"
            );
        }

        booking.setStatus(
                "CANCELLED"
        );

        Booking updatedBooking =
                bookingRepository.save(
                        booking
                );

        waitlistService.getNextUser(
                updatedBooking.getEquipmentId()
        );

        Notification notification =
                new Notification();

        notification.setUserId(
                updatedBooking.getUserId()
        );

        notification.setTitle(
                "Booking Cancelled"
        );

        notification.setMessage(
                "Your booking has been cancelled."
        );

        notification.setNotificationType(
                "BOOKING"
        );

        notification.setReferenceId(
                updatedBooking.getBookingId()
        );

        notification.setIsRead(false);

        notificationRepository.save(
                notification
        );

        User user =
                userRepository
                        .findById(
                                updatedBooking.getUserId()
                        )
                        .orElse(null);

        if (user != null) {

            emailService.sendEmail(
                    user.getEmail(),
                    "Booking Cancelled",
                    "Your booking has been cancelled."
            );

            notificationWebSocketService.sendNotification(
                    updatedBooking.getUserId(),
                    "Booking Cancelled",
                    "Your booking has been cancelled.",
                    "BOOKING"
            );
        }

        AuditLogRequestDTO audit =
                new AuditLogRequestDTO();

        audit.setUserId(
                updatedBooking.getUserId()
        );

        audit.setAction(
                "CANCEL"
        );

        audit.setModule(
                "BOOKING"
        );

        audit.setDescription(
                "Booking Cancelled"
        );

        audit.setIpAddress(
                "SYSTEM"
        );

        auditLogService.saveAuditLog(
                audit
        );

        return updatedBooking;
    }


    // =========================================================
    // MARK IN USE
    // =========================================================

    @Transactional
    public Booking markInUse(
            Integer id
    ) {

        Booking booking =
                getBookingById(id);

        if (!"APPROVED".equals(
                booking.getStatus()
        )) {

            throw new RuntimeException(
                    "Only approved bookings can be marked in use"
            );
        }

        /*
         * Start actual equipment usage tracking
         * before changing booking status.
         */
        equipmentUsageService.startUsage(
                booking.getBookingId(),
                booking.getEquipmentId(),
                booking.getUserId()
        );

        booking.setStatus(
                "IN_USE"
        );

        Booking updated =
                bookingRepository.save(
                        booking
                );

        Notification notification =
                new Notification();

        notification.setUserId(
                updated.getUserId()
        );

        notification.setTitle(
                "Equipment In Use"
        );

        notification.setMessage(
                "Your booked equipment is now marked as IN USE."
        );

        notification.setNotificationType(
                "BOOKING"
        );

        notification.setReferenceId(
                updated.getBookingId()
        );

        notification.setIsRead(false);

        notificationRepository.save(
                notification
        );

        User user =
                userRepository
                        .findById(
                                updated.getUserId()
                        )
                        .orElse(null);

        if (user != null) {

            emailService.sendEmail(
                    user.getEmail(),
                    "Equipment In Use",
                    "Your booked equipment is now in use."
            );
        }

        AuditLogRequestDTO audit =
                new AuditLogRequestDTO();

        audit.setUserId(
                updated.getUserId()
        );

        audit.setAction(
                "IN_USE"
        );

        audit.setModule(
                "BOOKING"
        );

        audit.setDescription(
                "Equipment marked IN USE"
        );

        audit.setIpAddress(
                "SYSTEM"
        );

        auditLogService.saveAuditLog(
                audit
        );

        return updated;
    }


    // =========================================================
    // COMPLETE BOOKING
    // =========================================================

    @Transactional
    public Booking markCompleted(
            Integer id
    ) {

        Booking booking =
                getBookingById(id);

        if (!"IN_USE".equals(
                booking.getStatus()
        )) {

            throw new RuntimeException(
                    "Only in-use bookings can be completed"
            );
        }

        /*
         * Stop actual equipment usage tracking
         * before completing the booking.
         */
        equipmentUsageService.stopUsage(
                booking.getBookingId()
        );

        booking.setStatus(
                "COMPLETED"
        );

        Booking updatedBooking =
                bookingRepository.save(
                        booking
                );


        // =====================================================
        // AUTO GENERATE INVOICE
        // =====================================================

        try {

            billingService.generateInvoice(
                    updatedBooking.getBookingId()
            );

        } catch (Exception e) {

            e.printStackTrace();
        }


        waitlistService.getNextUser(
                updatedBooking.getEquipmentId()
        );

        Notification notification =
                new Notification();

        notification.setUserId(
                updatedBooking.getUserId()
        );

        notification.setTitle(
                "Booking Completed"
        );

        notification.setMessage(
                "Your booking has been completed successfully."
        );

        notification.setNotificationType(
                "BOOKING"
        );

        notification.setReferenceId(
                updatedBooking.getBookingId()
        );

        notification.setIsRead(false);

        notificationRepository.save(
                notification
        );

        User user =
                userRepository
                        .findById(
                                updatedBooking.getUserId()
                        )
                        .orElse(null);

        if (user != null) {

            emailService.sendEmail(
                    user.getEmail(),
                    "Booking Completed",
                    "Your booking has been completed."
            );
        }

        notificationWebSocketService.sendNotification(
                updatedBooking.getUserId(),
                "Booking Completed",
                "Booking completed successfully.",
                "BOOKING"
        );

        AuditLogRequestDTO audit =
                new AuditLogRequestDTO();

        audit.setUserId(
                updatedBooking.getUserId()
        );

        audit.setAction(
                "COMPLETE"
        );

        audit.setModule(
                "BOOKING"
        );

        audit.setDescription(
                "Booking Completed"
        );

        audit.setIpAddress(
                "SYSTEM"
        );

        auditLogService.saveAuditLog(
                audit
        );

        return updatedBooking;
    }


    // =========================================================
    // NO SHOW
    // =========================================================

    public Booking markNoShow(
            Integer id
    ) {

        Booking booking =
                getBookingById(id);

        if (!"APPROVED".equals(
                booking.getStatus()
        )) {

            throw new RuntimeException(
                    "Only approved bookings can be marked no-show"
            );
        }

        booking.setStatus(
                "NO_SHOW"
        );

        Booking updated =
                bookingRepository.save(
                        booking
                );

        Notification notification =
                new Notification();

        notification.setUserId(
                updated.getUserId()
        );

        notification.setTitle(
                "No Show"
        );

        notification.setMessage(
                "You were marked as NO SHOW for your booking."
        );

        notification.setNotificationType(
                "BOOKING"
        );

        notification.setReferenceId(
                updated.getBookingId()
        );

        notification.setIsRead(false);

        notificationRepository.save(
                notification
        );

        User user =
                userRepository
                        .findById(
                                updated.getUserId()
                        )
                        .orElse(null);

        if (user != null) {

            emailService.sendEmail(
                    user.getEmail(),
                    "No Show",
                    "You were marked as NO SHOW for your booking."
            );
        }

        notificationWebSocketService.sendNotification(
                updated.getUserId(),
                "No Show",
                "You were marked as NO SHOW.",
                "BOOKING"
        );

        AuditLogRequestDTO audit =
                new AuditLogRequestDTO();

        audit.setUserId(
                updated.getUserId()
        );

        audit.setAction(
                "NO_SHOW"
        );

        audit.setModule(
                "BOOKING"
        );

        audit.setDescription(
                "Booking marked NO SHOW"
        );

        audit.setIpAddress(
                "SYSTEM"
        );

        auditLogService.saveAuditLog(
                audit
        );

        return updated;
    }
}