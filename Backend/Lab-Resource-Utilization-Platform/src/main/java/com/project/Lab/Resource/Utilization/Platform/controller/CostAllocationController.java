package com.project.Lab.Resource.Utilization.Platform.controller;

import com.project.Lab.Resource.Utilization.Platform.dto.CostAllocationRequestDTO;
import com.project.Lab.Resource.Utilization.Platform.entity.CostAllocation;
import com.project.Lab.Resource.Utilization.Platform.service.CostAllocationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cost-allocations")
public class CostAllocationController {

    @Autowired
    private CostAllocationService costAllocationService;


    // =========================================================
    // CREATE SINGLE COST ALLOCATION
    // =========================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','SYSTEM_ADMIN')")
    public ResponseEntity<CostAllocation> createAllocation(
            @RequestBody CostAllocationRequestDTO request
    ) {

        CostAllocation allocation =
                costAllocationService.createAllocation(request);

        return ResponseEntity.ok(allocation);
    }


    // =========================================================
    // CREATE COMPLETE COST SPLIT
    // =========================================================
    //
    // Example:
    // Institution A = 60%
    // Institution B = 40%
    //
    // Total must be exactly 100%.
    // =========================================================

    @PostMapping("/bill/{billId}/split")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','SYSTEM_ADMIN')")
    public ResponseEntity<List<CostAllocation>> createAllocations(
            @PathVariable Integer billId,
            @RequestBody List<CostAllocationRequestDTO> requests
    ) {

        List<CostAllocation> allocations =
                costAllocationService.createAllocations(
                        billId,
                        requests
                );

        return ResponseEntity.ok(allocations);
    }


    // =========================================================
    // GET ALL COST ALLOCATIONS
    // =========================================================

    @GetMapping
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','SYSTEM_ADMIN','DEPARTMENT_HEAD')")
    public ResponseEntity<List<CostAllocation>> getAll() {

        return ResponseEntity.ok(
                costAllocationService.getAll()
        );
    }


    // =========================================================
    // GET COST ALLOCATION BY ID
    // =========================================================

    @GetMapping("/{allocationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CostAllocation> getById(
            @PathVariable Integer allocationId
    ) {

        return ResponseEntity.ok(
                costAllocationService.getById(
                        allocationId
                )
        );
    }


    // =========================================================
    // GET ALLOCATIONS BY BILL
    // =========================================================

    @GetMapping("/bill/{billId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CostAllocation>> getByBill(
            @PathVariable Integer billId
    ) {

        return ResponseEntity.ok(
                costAllocationService.getByBill(
                        billId
                )
        );
    }


    // =========================================================
    // GET ALLOCATIONS BY BOOKING
    // =========================================================

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CostAllocation>> getByBooking(
            @PathVariable Integer bookingId
    ) {

        return ResponseEntity.ok(
                costAllocationService.getByBooking(
                        bookingId
                )
        );
    }


    // =========================================================
    // GET ALLOCATIONS BY INSTITUTION
    // =========================================================

    @GetMapping("/institution/{institutionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CostAllocation>> getByInstitution(
            @PathVariable Integer institutionId
    ) {

        return ResponseEntity.ok(
                costAllocationService.getByInstitution(
                        institutionId
                )
        );
    }


    // =========================================================
    // GET ALLOCATIONS BY RESOURCE SHARE
    // =========================================================

    @GetMapping("/resource-share/{resourceShareId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CostAllocation>> getByResourceShare(
            @PathVariable Integer resourceShareId
    ) {

        return ResponseEntity.ok(
                costAllocationService.getByResourceShare(
                        resourceShareId
                )
        );
    }


    // =========================================================
    // GET ALLOCATIONS BY STATUS
    // =========================================================

    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CostAllocation>> getByStatus(
            @PathVariable String status
    ) {

        return ResponseEntity.ok(
                costAllocationService.getByStatus(
                        status
                )
        );
    }


    // =========================================================
    // APPROVE COST ALLOCATION
    // =========================================================

    @PutMapping("/{allocationId}/approve")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','SYSTEM_ADMIN')")
    public ResponseEntity<CostAllocation> approve(
            @PathVariable Integer allocationId
    ) {

        return ResponseEntity.ok(
                costAllocationService.approve(
                        allocationId
                )
        );
    }


    // =========================================================
    // MARK COST ALLOCATION AS PAID
    // =========================================================

    @PutMapping("/{allocationId}/pay")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','SYSTEM_ADMIN')")
    public ResponseEntity<CostAllocation> markAsPaid(
            @PathVariable Integer allocationId
    ) {

        return ResponseEntity.ok(
                costAllocationService.markAsPaid(
                        allocationId
                )
        );
    }


    // =========================================================
    // CANCEL COST ALLOCATION
    // =========================================================

    @PutMapping("/{allocationId}/cancel")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','SYSTEM_ADMIN')")
    public ResponseEntity<CostAllocation> cancel(
            @PathVariable Integer allocationId,
            @RequestParam(required = false) String remarks
    ) {

        return ResponseEntity.ok(
                costAllocationService.cancel(
                        allocationId,
                        remarks
                )
        );
    }


    // =========================================================
    // GET TOTAL ALLOCATION PERCENTAGE
    // =========================================================

    @GetMapping("/bill/{billId}/total-percentage")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> getTotalAllocationPercentage(
            @PathVariable Integer billId
    ) {

        BigDecimal total =
                costAllocationService
                        .getTotalAllocationPercentage(
                                billId
                        );

        return ResponseEntity.ok(total);
    }


    // =========================================================
    // CHECK WHETHER BILL IS FULLY ALLOCATED
    // =========================================================

    @GetMapping("/bill/{billId}/fully-allocated")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> isFullyAllocated(
            @PathVariable Integer billId
    ) {

        boolean fullyAllocated =
                costAllocationService.isFullyAllocated(
                        billId
                );

        return ResponseEntity.ok(fullyAllocated);
    }
}