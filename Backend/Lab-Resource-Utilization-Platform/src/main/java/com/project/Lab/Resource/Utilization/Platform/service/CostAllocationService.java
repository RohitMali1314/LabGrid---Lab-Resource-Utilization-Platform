package com.project.Lab.Resource.Utilization.Platform.service;

import com.project.Lab.Resource.Utilization.Platform.dto.CostAllocationRequestDTO;
import com.project.Lab.Resource.Utilization.Platform.entity.Billing;
import com.project.Lab.Resource.Utilization.Platform.entity.CostAllocation;
import com.project.Lab.Resource.Utilization.Platform.entity.ResourceShare;
import com.project.Lab.Resource.Utilization.Platform.repository.BillingRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.CostAllocationRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.ResourceShareRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CostAllocationService {

    @Autowired
    private CostAllocationRepository costAllocationRepository;

    @Autowired
    private BillingRepository billingRepository;

    @Autowired
    private ResourceShareRepository resourceShareRepository;


    // =========================================================
    // CREATE SINGLE COST ALLOCATION
    // =========================================================

    @Transactional
    public CostAllocation createAllocation(
            CostAllocationRequestDTO dto
    ) {

        if (dto == null) {
            throw new RuntimeException(
                    "Cost allocation request cannot be empty."
            );
        }

        if (dto.getBillId() == null) {
            throw new RuntimeException(
                    "Bill ID is required."
            );
        }

        if (dto.getInstitutionId() == null) {
            throw new RuntimeException(
                    "Institution ID is required."
            );
        }

        if (dto.getAllocationPercentage() == null) {
            throw new RuntimeException(
                    "Allocation percentage is required."
            );
        }

        validatePercentage(
                dto.getAllocationPercentage()
        );

        Billing billing = billingRepository
                .findById(dto.getBillId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bill not found."
                        )
                );

        if ("CANCELLED".equalsIgnoreCase(
                billing.getStatus()
        )) {
            throw new RuntimeException(
                    "Cannot allocate cost for a cancelled bill."
            );
        }

        if (dto.getBookingId() != null &&
                !dto.getBookingId().equals(
                        billing.getBookingId()
                )) {

            throw new RuntimeException(
                    "Booking does not belong to this bill."
            );
        }

        ResourceShare resourceShare = null;

        if (dto.getResourceShareId() != null) {

            resourceShare =
                    resourceShareRepository
                            .findById(
                                    dto.getResourceShareId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Resource share not found."
                                    )
                            );

            validateResourceShare(
                    resourceShare,
                    dto.getInstitutionId()
            );
        }

        if (costAllocationRepository
                .existsByBillIdAndInstitutionId(
                        dto.getBillId(),
                        dto.getInstitutionId()
                )) {

            throw new RuntimeException(
                    "Cost allocation already exists for this institution."
            );
        }

        BigDecimal existingPercentage =
                getTotalAllocationPercentage(
                        dto.getBillId()
                );

        BigDecimal totalPercentage =
                existingPercentage.add(
                        dto.getAllocationPercentage()
                );

        if (totalPercentage.compareTo(
                new BigDecimal("100")
        ) > 0) {

            throw new RuntimeException(
                    "Total allocation percentage cannot exceed 100%. "
                            + "Already allocated: "
                            + existingPercentage
                            + "%"
            );
        }

        BigDecimal allocatedAmount =
                calculateAmount(
                        billing.getGrandTotal(),
                        dto.getAllocationPercentage()
                );

        CostAllocation allocation =
                new CostAllocation();

        allocation.setBillId(
                dto.getBillId()
        );

        allocation.setBookingId(
                dto.getBookingId() != null
                        ? dto.getBookingId()
                        : billing.getBookingId()
        );

        allocation.setResourceShareId(
                dto.getResourceShareId()
        );

        allocation.setInstitutionId(
                dto.getInstitutionId()
        );

        allocation.setAllocationPercentage(
                dto.getAllocationPercentage()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
        );

        allocation.setAllocatedAmount(
                allocatedAmount
        );

        allocation.setStatus(
                "PENDING"
        );

        allocation.setRemarks(
                dto.getRemarks()
        );

        allocation.setCreatedAt(
                LocalDateTime.now()
        );

        return costAllocationRepository.save(
                allocation
        );
    }


    // =========================================================
    // CREATE COMPLETE COST SPLIT
    // =========================================================

    @Transactional
    public List<CostAllocation> createAllocations(
            Integer billId,
            List<CostAllocationRequestDTO> requests
    ) {

        if (billId == null) {
            throw new RuntimeException(
                    "Bill ID is required."
            );
        }

        if (requests == null ||
                requests.isEmpty()) {

            throw new RuntimeException(
                    "At least one cost allocation is required."
            );
        }

        Billing billing = billingRepository
                .findById(billId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bill not found."
                        )
                );

        if ("CANCELLED".equalsIgnoreCase(
                billing.getStatus()
        )) {
            throw new RuntimeException(
                    "Cannot allocate cost for a cancelled bill."
            );
        }

        BigDecimal totalPercentage =
                BigDecimal.ZERO;

        for (CostAllocationRequestDTO dto : requests) {

            if (dto == null) {
                throw new RuntimeException(
                        "Invalid allocation request."
                );
            }

            if (dto.getInstitutionId() == null) {
                throw new RuntimeException(
                        "Institution ID is required."
                );
            }

            if (dto.getAllocationPercentage() == null) {
                throw new RuntimeException(
                        "Allocation percentage is required."
                );
            }

            validatePercentage(
                    dto.getAllocationPercentage()
            );

            totalPercentage =
                    totalPercentage.add(
                            dto.getAllocationPercentage()
                    );

            if (dto.getBookingId() != null &&
                    !dto.getBookingId().equals(
                            billing.getBookingId()
                    )) {

                throw new RuntimeException(
                        "Booking does not belong to this bill."
                );
            }

            if (dto.getResourceShareId() == null) {
                throw new RuntimeException(
                        "Resource share ID is required."
                );
            }

            ResourceShare resourceShare =
                    resourceShareRepository
                            .findById(
                                    dto.getResourceShareId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Resource share not found."
                                    )
                            );

            validateResourceShare(
                    resourceShare,
                    dto.getInstitutionId()
            );
        }

        if (totalPercentage.compareTo(
                new BigDecimal("100")
        ) != 0) {

            throw new RuntimeException(
                    "Total allocation percentage must be exactly 100%. "
                            + "Current total: "
                            + totalPercentage
                            + "%"
            );
        }

        costAllocationRepository
                .deleteByBillId(billId);

        return requests.stream()
                .map(dto -> {

                    CostAllocation allocation =
                            new CostAllocation();

                    allocation.setBillId(
                            billId
                    );

                    allocation.setBookingId(
                            dto.getBookingId() != null
                                    ? dto.getBookingId()
                                    : billing.getBookingId()
                    );

                    allocation.setResourceShareId(
                            dto.getResourceShareId()
                    );

                    allocation.setInstitutionId(
                            dto.getInstitutionId()
                    );

                    allocation.setAllocationPercentage(
                            dto.getAllocationPercentage()
                                    .setScale(
                                            2,
                                            RoundingMode.HALF_UP
                                    )
                    );

                    allocation.setAllocatedAmount(
                            calculateAmount(
                                    billing.getGrandTotal(),
                                    dto.getAllocationPercentage()
                            )
                    );

                    allocation.setStatus(
                            "PENDING"
                    );

                    allocation.setRemarks(
                            dto.getRemarks()
                    );

                    allocation.setCreatedAt(
                            LocalDateTime.now()
                    );

                    return costAllocationRepository.save(
                            allocation
                    );
                })
                .toList();
    }


    // =========================================================
    // GET ALL
    // =========================================================

    public List<CostAllocation> getAll() {

        return costAllocationRepository.findAll();
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    public CostAllocation getById(
            Integer allocationId
    ) {

        return costAllocationRepository
                .findById(allocationId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Cost allocation not found."
                        )
                );
    }


    // =========================================================
    // GET BY BILL
    // =========================================================

    public List<CostAllocation> getByBill(
            Integer billId
    ) {

        return costAllocationRepository
                .findByBillId(billId);
    }


    // =========================================================
    // GET BY BOOKING
    // =========================================================

    public List<CostAllocation> getByBooking(
            Integer bookingId
    ) {

        return costAllocationRepository
                .findByBookingId(bookingId);
    }


    // =========================================================
    // GET BY INSTITUTION
    // =========================================================

    public List<CostAllocation> getByInstitution(
            Integer institutionId
    ) {

        return costAllocationRepository
                .findByInstitutionId(institutionId);
    }


    // =========================================================
    // GET BY RESOURCE SHARE
    // =========================================================

    public List<CostAllocation> getByResourceShare(
            Integer resourceShareId
    ) {

        return costAllocationRepository
                .findByResourceShareId(
                        resourceShareId
                );
    }


    // =========================================================
    // GET BY STATUS
    // =========================================================

    public List<CostAllocation> getByStatus(
            String status
    ) {

        if (status == null ||
                status.trim().isEmpty()) {

            throw new RuntimeException(
                    "Status is required."
            );
        }

        return costAllocationRepository
                .findByStatus(
                        status.toUpperCase()
                );
    }


    // =========================================================
    // APPROVE
    // =========================================================

    @Transactional
    public CostAllocation approve(
            Integer allocationId
    ) {

        CostAllocation allocation =
                getById(allocationId);

        if ("CANCELLED".equalsIgnoreCase(
                allocation.getStatus()
        )) {

            throw new RuntimeException(
                    "Cancelled allocation cannot be approved."
            );
        }

        allocation.setStatus(
                "APPROVED"
        );

        return costAllocationRepository.save(
                allocation
        );
    }


    // =========================================================
    // MARK AS PAID
    // =========================================================

    @Transactional
    public CostAllocation markAsPaid(
            Integer allocationId
    ) {

        CostAllocation allocation =
                getById(allocationId);

        if (!"APPROVED".equalsIgnoreCase(
                allocation.getStatus()
        )) {

            throw new RuntimeException(
                    "Only approved allocation can be marked as paid."
            );
        }

        allocation.setStatus(
                "PAID"
        );

        return costAllocationRepository.save(
                allocation
        );
    }


    // =========================================================
    // CANCEL
    // =========================================================

    @Transactional
    public CostAllocation cancel(
            Integer allocationId,
            String remarks
    ) {

        CostAllocation allocation =
                getById(allocationId);

        if ("PAID".equalsIgnoreCase(
                allocation.getStatus()
        )) {

            throw new RuntimeException(
                    "Paid allocation cannot be cancelled."
            );
        }

        allocation.setStatus(
                "CANCELLED"
        );

        if (remarks != null &&
                !remarks.trim().isEmpty()) {

            allocation.setRemarks(
                    remarks.trim()
            );
        }

        return costAllocationRepository.save(
                allocation
        );
    }


    // =========================================================
    // TOTAL ALLOCATION %
    // =========================================================

    public BigDecimal getTotalAllocationPercentage(
            Integer billId
    ) {

        List<CostAllocation> allocations =
                costAllocationRepository
                        .findByBillId(billId);

        return allocations.stream()
                .filter(a ->
                        !"CANCELLED".equalsIgnoreCase(
                                a.getStatus()
                        )
                )
                .map(
                        CostAllocation
                                ::getAllocationPercentage
                )
                .filter(p -> p != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }


    // =========================================================
    // CHECK FULLY ALLOCATED
    // =========================================================

    public boolean isFullyAllocated(
            Integer billId
    ) {

        return getTotalAllocationPercentage(
                billId
        ).compareTo(
                new BigDecimal("100")
        ) == 0;
    }


    // =========================================================
    // VALIDATE RESOURCE SHARE
    // =========================================================

    private void validateResourceShare(
            ResourceShare resourceShare,
            Integer institutionId
    ) {

        if (!"ACTIVE".equalsIgnoreCase(
                resourceShare.getStatus()
        )) {

            throw new RuntimeException(
                    "Cost allocation requires an ACTIVE resource share."
            );
        }

        boolean institutionPartOfShare =
                institutionId.equals(
                        resourceShare
                                .getOwnerInstitutionId()
                )
                        ||
                        institutionId.equals(
                                resourceShare
                                        .getTargetInstitutionId()
                        );

        if (!institutionPartOfShare) {

            throw new RuntimeException(
                    "Institution is not part of this resource sharing agreement."
            );
        }
    }


    // =========================================================
    // VALIDATE PERCENTAGE
    // =========================================================

    private void validatePercentage(
            BigDecimal percentage
    ) {

        if (percentage.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new RuntimeException(
                    "Allocation percentage must be greater than 0."
            );
        }

        if (percentage.compareTo(
                new BigDecimal("100")
        ) > 0) {

            throw new RuntimeException(
                    "Allocation percentage cannot exceed 100%."
            );
        }
    }


    // =========================================================
    // CALCULATE ALLOCATED AMOUNT
    // =========================================================

    private BigDecimal calculateAmount(
            BigDecimal grandTotal,
            BigDecimal percentage
    ) {

        if (grandTotal == null) {
            grandTotal = BigDecimal.ZERO;
        }

        return grandTotal
                .multiply(percentage)
                .divide(
                        new BigDecimal("100"),
                        2,
                        RoundingMode.HALF_UP
                );
    }
}