package com.project.Lab.Resource.Utilization.Platform.repository;

import com.project.Lab.Resource.Utilization.Platform.entity.CostAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CostAllocationRepository
        extends JpaRepository<CostAllocation, Integer> {

    List<CostAllocation> findByBillId(Integer billId);

    List<CostAllocation> findByBookingId(Integer bookingId);

    List<CostAllocation> findByInstitutionId(Integer institutionId);

    List<CostAllocation> findByResourceShareId(
            Integer resourceShareId
    );

    List<CostAllocation> findByStatus(String status);

    Optional<CostAllocation>
    findByBillIdAndInstitutionId(
            Integer billId,
            Integer institutionId
    );

    boolean existsByBillIdAndInstitutionId(
            Integer billId,
            Integer institutionId
    );

    void deleteByBillId(Integer billId);
}