package com.project.Lab.Resource.Utilization.Platform.service;

import com.project.Lab.Resource.Utilization.Platform.dto.CostAllocationRequestDTO;
import com.project.Lab.Resource.Utilization.Platform.entity.*;
import com.project.Lab.Resource.Utilization.Platform.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BillingService {

    @Autowired
    private BillingRepository billingRepository;

    @Autowired
    private BillingItemRepository billingItemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private CostAllocationService costAllocationService;


    // =========================================================
    // GENERATE INVOICE
    // =========================================================

    @Transactional
    public Billing generateInvoice(Integer bookingId) {

        if (bookingId == null) {
            throw new RuntimeException(
                    "Booking ID is required"
            );
        }

        // ---------------------------------------------------------
        // If invoice already exists, return existing invoice
        // ---------------------------------------------------------

        if (billingRepository.existsByBookingId(bookingId)) {

            return billingRepository
                    .findByBookingId(bookingId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Invoice not found"
                            )
                    );
        }

        // ---------------------------------------------------------
        // Get booking
        // ---------------------------------------------------------

        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Booking not found"
                        )
                );

        if (booking.getStartTime() == null
                || booking.getEndTime() == null) {

            throw new RuntimeException(
                    "Booking start time and end time are required"
            );
        }

        if (!booking.getEndTime()
                .isAfter(booking.getStartTime())) {

            throw new RuntimeException(
                    "Booking end time must be after start time"
            );
        }

        // ---------------------------------------------------------
        // Get user
        // ---------------------------------------------------------

        User user = userRepository
                .findById(booking.getUserId())
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

        // ---------------------------------------------------------
        // Get equipment
        // ---------------------------------------------------------

        Equipment equipment = equipmentRepository
                .findById(booking.getEquipmentId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Equipment not found"
                        )
                );

        // ---------------------------------------------------------
        // Get institution
        // ---------------------------------------------------------

        Institution institution = institutionRepository
                .findById(user.getInstitutionId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Institution not found"
                        )
                );

        // ---------------------------------------------------------
        // Calculate usage hours
        // ---------------------------------------------------------

        long minutes = Duration.between(
                booking.getStartTime(),
                booking.getEndTime()
        ).toMinutes();

        if (minutes <= 0) {

            throw new RuntimeException(
                    "Booking duration must be greater than zero"
            );
        }

        BigDecimal hours =
                BigDecimal.valueOf(minutes)
                        .divide(
                                BigDecimal.valueOf(60),
                                2,
                                RoundingMode.HALF_UP
                        );

        // ---------------------------------------------------------
        // Hourly rate
        // ---------------------------------------------------------

        BigDecimal hourlyRate =
                equipment.getHourlyRate();

        if (hourlyRate == null) {
            hourlyRate = BigDecimal.ZERO;
        }

        hourlyRate = hourlyRate.setScale(
                2,
                RoundingMode.HALF_UP
        );

        // ---------------------------------------------------------
        // Subtotal
        // ---------------------------------------------------------

        BigDecimal subtotal =
                hourlyRate
                        .multiply(hours)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        // ---------------------------------------------------------
        // GST 18%
        // ---------------------------------------------------------

        BigDecimal gst =
                subtotal
                        .multiply(
                                new BigDecimal("0.18")
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        // ---------------------------------------------------------
        // Grand total
        // ---------------------------------------------------------

        BigDecimal grandTotal =
                subtotal
                        .add(gst)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        // ---------------------------------------------------------
        // Invoice number
        // ---------------------------------------------------------

        String invoiceNumber =
                "INV-" + System.currentTimeMillis();

        // ---------------------------------------------------------
        // Create Billing
        // ---------------------------------------------------------

        Billing billing = new Billing();

        billing.setBookingId(
                booking.getBookingId()
        );

        billing.setInstitutionId(
                institution.getInstitutionId()
        );

        billing.setInvoiceNumber(
                invoiceNumber
        );

        billing.setSubtotal(
                subtotal
        );

        billing.setGst(
                gst
        );

        billing.setGrandTotal(
                grandTotal
        );

        billing.setStatus(
                "PENDING"
        );

        billing.setGeneratedDate(
                LocalDateTime.now()
        );

        billing.setCreatedAt(
                LocalDateTime.now()
        );

        billing.setDueDate(
                LocalDateTime.now()
                        .plusDays(15)
        );

        Billing savedBilling =
                billingRepository.save(
                        billing
                );

        // ---------------------------------------------------------
        // Create Billing Item
        // ---------------------------------------------------------

        BillingItem item =
                new BillingItem();

        item.setBillId(
                savedBilling.getBillId()
        );

        item.setEquipmentId(
                equipment.getEquipmentId()
        );

        item.setDescription(
                equipment.getEquipmentName()
        );

        item.setHoursUsed(
                hours
        );

        item.setHourlyRate(
                hourlyRate
        );

        item.setAmount(
                subtotal
        );

        billingItemRepository.save(
                item
        );

        return savedBilling;
    }


    // =========================================================
    // GET ALL BILLS
    // =========================================================

    public List<Billing> getAllBills() {

        return billingRepository.findAll();
    }


    // =========================================================
    // GET BILL BY ID
    // =========================================================

    public Billing getBillById(
            Integer billId
    ) {

        return billingRepository
                .findById(billId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bill not found"
                        )
                );
    }


    // =========================================================
    // GET BILLS BY INSTITUTION
    // =========================================================

    public List<Billing> getInstitutionBills(
            Integer institutionId
    ) {

        return billingRepository
                .findByInstitutionId(
                        institutionId
                );
    }


    // =========================================================
    // PAY INVOICE
    // =========================================================

    @Transactional
    public Billing payInvoice(
            Integer billId
    ) {

        Billing billing =
                getBillById(billId);

        if ("PAID".equalsIgnoreCase(
                billing.getStatus()
        )) {

            throw new RuntimeException(
                    "Invoice is already paid"
            );
        }

        if ("CANCELLED".equalsIgnoreCase(
                billing.getStatus()
        )) {

            throw new RuntimeException(
                    "Cancelled invoice cannot be paid"
            );
        }

        billing.setStatus(
                "PAID"
        );

        billing.setPaidDate(
                LocalDateTime.now()
        );

        return billingRepository.save(
                billing
        );
    }


    // =========================================================
    // CANCEL INVOICE
    // =========================================================

    @Transactional
    public Billing cancelInvoice(
            Integer billId
    ) {

        Billing billing =
                getBillById(billId);

        if ("PAID".equalsIgnoreCase(
                billing.getStatus()
        )) {

            throw new RuntimeException(
                    "Paid invoice cannot be cancelled"
            );
        }

        billing.setStatus(
                "CANCELLED"
        );

        return billingRepository.save(
                billing
        );
    }


    // =========================================================
    // DELETE BILL
    // =========================================================

    @Transactional
    public void deleteBill(
            Integer billId
    ) {

        Billing billing =
                getBillById(billId);

        billingItemRepository
                .findByBillId(billId)
                .forEach(item ->
                        billingItemRepository.delete(item)
                );

        billingRepository.delete(
                billing
        );
    }


    // =========================================================
    // CREATE SINGLE COST ALLOCATION
    // =========================================================
    //
    // Example:
    //
    // Bill = ₹1,180
    // Institution A = 60%
    // Institution B = 40%
    //
    // A = ₹708
    // B = ₹472
    //
    // Total = ₹1,180
    //
    // =========================================================

    @Transactional
    public CostAllocation createCostAllocation(
            Integer billId,
            Integer institutionId,
            BigDecimal percentage,
            Integer resourceShareId,
            String remarks
    ) {

        Billing billing =
                getBillById(billId);

        if ("CANCELLED".equalsIgnoreCase(
                billing.getStatus()
        )) {

            throw new RuntimeException(
                    "Cannot allocate cost for a cancelled bill"
            );
        }

        if (institutionId == null) {

            throw new RuntimeException(
                    "Institution ID is required"
            );
        }

        if (percentage == null) {

            throw new RuntimeException(
                    "Allocation percentage is required"
            );
        }

        return costAllocationService
                .createAllocation(
                        createAllocationDTO(
                                billId,
                                billing.getBookingId(),
                                institutionId,
                                percentage,
                                resourceShareId,
                                remarks
                        )
                );
    }


    // =========================================================
    // CREATE COMPLETE COST SPLIT
    // =========================================================
    //
    // The supplied percentages must total exactly 100%.
    //
    // Example:
    //
    // Institution A = 60%
    // Institution B = 40%
    //
    // =========================================================

    @Transactional
    public List<CostAllocation> createCostAllocations(
            Integer billId,
            List<CostAllocationRequestDTO> requests
    ) {

        Billing billing =
                getBillById(billId);

        if ("CANCELLED".equalsIgnoreCase(
                billing.getStatus()
        )) {

            throw new RuntimeException(
                    "Cannot allocate cost for a cancelled bill"
            );
        }

        if (requests == null
                || requests.isEmpty()) {

            throw new RuntimeException(
                    "At least one allocation is required"
            );
        }

        // ---------------------------------------------------------
        // Automatically attach bill ID and booking ID
        // ---------------------------------------------------------

        for (CostAllocationRequestDTO request : requests) {

            if (request == null) {

                throw new RuntimeException(
                        "Invalid allocation request"
                );
            }

            request.setBillId(
                    billId
            );

            if (request.getBookingId() == null) {

                request.setBookingId(
                        billing.getBookingId()
                );
            }
        }

        return costAllocationService
                .createAllocations(
                        billId,
                        requests
                );
    }


    // =========================================================
    // GET COST ALLOCATIONS FOR BILL
    // =========================================================

    public List<CostAllocation> getCostAllocations(
            Integer billId
    ) {

        getBillById(billId);

        return costAllocationService
                .getByBill(
                        billId
                );
    }


    // =========================================================
    // GET COST ALLOCATIONS FOR BOOKING
    // =========================================================

    public List<CostAllocation> getBookingCostAllocations(
            Integer bookingId
    ) {

        return costAllocationService
                .getByBooking(
                        bookingId
                );
    }


    // =========================================================
    // CREATE COST ALLOCATION DTO
    // =========================================================

    private CostAllocationRequestDTO createAllocationDTO(
            Integer billId,
            Integer bookingId,
            Integer institutionId,
            BigDecimal percentage,
            Integer resourceShareId,
            String remarks
    ) {

        CostAllocationRequestDTO dto =
                new CostAllocationRequestDTO();

        dto.setBillId(
                billId
        );

        dto.setBookingId(
                bookingId
        );

        dto.setInstitutionId(
                institutionId
        );

        dto.setAllocationPercentage(
                percentage
        );

        dto.setResourceShareId(
                resourceShareId
        );

        dto.setRemarks(
                remarks
        );

        return dto;
    }
}