package com.project.Lab.Resource.Utilization.Platform.repository;

import com.project.Lab.Resource.Utilization.Platform.entity.ResourceShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ResourceShareRepository
        extends JpaRepository<ResourceShare, Integer> {

    List<ResourceShare> findByOwnerInstitutionId(
            Integer institutionId
    );

    List<ResourceShare> findByTargetInstitutionId(
            Integer institutionId
    );

    List<ResourceShare> findByEquipmentId(
            Integer equipmentId
    );

    List<ResourceShare> findByStatus(
            String status
    );

    List<ResourceShare> findByOwnerInstitutionIdAndStatus(
            Integer institutionId,
            String status
    );

    List<ResourceShare> findByTargetInstitutionIdAndStatus(
            Integer institutionId,
            String status
    );

    List<ResourceShare> findByEquipmentIdAndStatus(
            Integer equipmentId,
            String status
    );

    boolean existsByEquipmentIdAndTargetInstitutionIdAndStatus(
            Integer equipmentId,
            Integer targetInstitutionId,
            String status
    );

    List<ResourceShare>
    findByEquipmentIdAndTargetInstitutionIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Integer equipmentId,
            Integer targetInstitutionId,
            String status,
            LocalDate startDate,
            LocalDate endDate
    );

    // =========================================================
    // ACTIVE SHARING FOR EQUIPMENT
    // =========================================================

    List<ResourceShare> findByEquipmentIdAndStatusOrderByStartDateAsc(
            Integer equipmentId,
            String status
    );

    // =========================================================
    // ACTIVE SHARING BETWEEN INSTITUTIONS
    // =========================================================

    List<ResourceShare>
    findByOwnerInstitutionIdAndTargetInstitutionIdAndStatus(
            Integer ownerInstitutionId,
            Integer targetInstitutionId,
            String status
    );
}