package com.project.Lab.Resource.Utilization.Platform.repository;

import com.project.Lab.Resource.Utilization.Platform.entity.Billing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillingRepository extends JpaRepository<Billing, Integer> {

    List<Billing> findByInstitutionId(Integer institutionId);

    List<Billing> findByStatus(String status);

    Billing findByInvoiceNumber(String invoiceNumber);
    boolean existsByBookingId(Integer bookingId);
    Optional<Billing> findByBookingId(Integer bookingId);

}
