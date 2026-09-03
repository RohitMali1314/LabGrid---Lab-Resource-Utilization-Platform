package com.project.Lab.Resource.Utilization.Platform.controller;

import com.project.Lab.Resource.Utilization.Platform.dto.CostAllocationRequestDTO;
import com.project.Lab.Resource.Utilization.Platform.entity.Billing;
import com.project.Lab.Resource.Utilization.Platform.entity.CostAllocation;
import com.project.Lab.Resource.Utilization.Platform.service.BillingService;
import com.project.Lab.Resource.Utilization.Platform.service.CostAllocationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
@CrossOrigin(origins = "*")
public class BillingController {

    @Autowired
    private BillingService billingService;

    @Autowired
    private CostAllocationService costAllocationService;


    // =========================================================
    // GENERATE INVOICE
    // =========================================================

    @PostMapping("/generate/{bookingId}")
    public ResponseEntity<Billing> generateInvoice(
            @PathVariable Integer bookingId) {

        return ResponseEntity.ok(
                billingService.generateInvoice(bookingId)
        );
    }


    // =========================================================
    // GET ALL BILLS
    // =========================================================

    @GetMapping
    public ResponseEntity<List<Billing>> getAllBills() {

        return ResponseEntity.ok(
                billingService.getAllBills()
        );
    }


    // =========================================================
    // GET BILL BY ID
    // =========================================================

    @GetMapping("/{billId}")
    public ResponseEntity<Billing> getBill(
            @PathVariable Integer billId) {

        return ResponseEntity.ok(
                billingService.getBillById(billId)
        );
    }


    // =========================================================
    // GET BILLS BY INSTITUTION
    // =========================================================

    @GetMapping("/institution/{institutionId}")
    public ResponseEntity<List<Billing>> getInstitutionBills(
            @PathVariable Integer institutionId) {

        return ResponseEntity.ok(
                billingService.getInstitutionBills(
                        institutionId
                )
        );
    }


    // =========================================================
    // PAY INVOICE
    // =========================================================

    @PutMapping("/pay/{billId}")
    public ResponseEntity<Billing> payInvoice(
            @PathVariable Integer billId) {

        return ResponseEntity.ok(
                billingService.payInvoice(
                        billId
                )
        );
    }


    // =========================================================
    // CANCEL INVOICE
    // =========================================================

    @PutMapping("/cancel/{billId}")
    public ResponseEntity<Billing> cancelInvoice(
            @PathVariable Integer billId) {

        return ResponseEntity.ok(
                billingService.cancelInvoice(
                        billId
                )
        );
    }


    // =========================================================
    // DELETE INVOICE
    // =========================================================

    @DeleteMapping("/{billId}")
    public ResponseEntity<String> deleteBill(
            @PathVariable Integer billId) {

        billingService.deleteBill(
                billId
        );

        return ResponseEntity.ok(
                "Invoice deleted successfully"
        );
    }


    // =========================================================
    // CREATE SINGLE COST ALLOCATION
    // =========================================================

    @PostMapping("/{billId}/allocations")
    public ResponseEntity<CostAllocation> createCostAllocation(
            @PathVariable Integer billId,
            @RequestBody CostAllocationRequestDTO dto) {

        dto.setBillId(billId);

        return ResponseEntity.ok(
                costAllocationService.createAllocation(dto)
        );
    }


    // =========================================================
    // CREATE COMPLETE COST SPLIT
    // =========================================================

    @PostMapping("/{billId}/allocations/bulk")
    public ResponseEntity<List<CostAllocation>>
    createCostAllocations(
            @PathVariable Integer billId,
            @RequestBody List<CostAllocationRequestDTO> requests) {

        for (CostAllocationRequestDTO dto : requests) {
            dto.setBillId(billId);
        }

        return ResponseEntity.ok(
                costAllocationService.createAllocations(
                        billId,
                        requests
                )
        );
    }


    // =========================================================
    // GET ALLOCATIONS FOR BILL
    // =========================================================

    @GetMapping("/{billId}/allocations")
    public ResponseEntity<List<CostAllocation>>
    getCostAllocations(
            @PathVariable Integer billId) {

        return ResponseEntity.ok(
                costAllocationService.getByBill(
                        billId
                )
        );
    }


    // =========================================================
    // GET ALLOCATION BY ID
    // =========================================================

    @GetMapping("/allocations/{allocationId}")
    public ResponseEntity<CostAllocation>
    getAllocationById(
            @PathVariable Integer allocationId) {

        return ResponseEntity.ok(
                costAllocationService.getById(
                        allocationId
                )
        );
    }


    // =========================================================
    // GET ALLOCATIONS BY BOOKING
    // =========================================================

    @GetMapping("/booking/{bookingId}/allocations")
    public ResponseEntity<List<CostAllocation>>
    getBookingCostAllocations(
            @PathVariable Integer bookingId) {

        return ResponseEntity.ok(
                costAllocationService.getByBooking(
                        bookingId
                )
        );
    }


    // =========================================================
    // GET ALLOCATIONS BY INSTITUTION
    // =========================================================

    @GetMapping("/allocations/institution/{institutionId}")
    public ResponseEntity<List<CostAllocation>>
    getAllocationsByInstitution(
            @PathVariable Integer institutionId) {

        return ResponseEntity.ok(
                costAllocationService.getByInstitution(
                        institutionId
                )
        );
    }


    // =========================================================
    // GET ALLOCATIONS BY RESOURCE SHARE
    // =========================================================

    @GetMapping(
            "/allocations/resource-share/{resourceShareId}"
    )
    public ResponseEntity<List<CostAllocation>>
    getAllocationsByResourceShare(
            @PathVariable Integer resourceShareId) {

        return ResponseEntity.ok(
                costAllocationService.getByResourceShare(
                        resourceShareId
                )
        );
    }


    // =========================================================
    // GET ALLOCATIONS BY STATUS
    // =========================================================

    @GetMapping("/allocations/status/{status}")
    public ResponseEntity<List<CostAllocation>>
    getAllocationsByStatus(
            @PathVariable String status) {

        return ResponseEntity.ok(
                costAllocationService.getByStatus(
                        status
                )
        );
    }


    // =========================================================
    // APPROVE ALLOCATION
    // =========================================================

    @PutMapping("/allocations/{allocationId}/approve")
    public ResponseEntity<CostAllocation>
    approveAllocation(
            @PathVariable Integer allocationId) {

        return ResponseEntity.ok(
                costAllocationService.approve(
                        allocationId
                )
        );
    }


    // =========================================================
    // MARK ALLOCATION PAID
    // =========================================================

    @PutMapping("/allocations/{allocationId}/pay")
    public ResponseEntity<CostAllocation>
    payAllocation(
            @PathVariable Integer allocationId) {

        return ResponseEntity.ok(
                costAllocationService.markAsPaid(
                        allocationId
                )
        );
    }


    // =========================================================
    // CANCEL ALLOCATION
    // =========================================================

    @PutMapping("/allocations/{allocationId}/cancel")
    public ResponseEntity<CostAllocation>
    cancelAllocation(
            @PathVariable Integer allocationId,
            @RequestParam(required = false)
            String remarks) {

        return ResponseEntity.ok(
                costAllocationService.cancel(
                        allocationId,
                        remarks
                )
        );
    }
}