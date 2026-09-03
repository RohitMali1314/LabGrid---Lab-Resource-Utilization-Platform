package com.project.Lab.Resource.Utilization.Platform.service;

import com.project.Lab.Resource.Utilization.Platform.entity.Booking;
import com.project.Lab.Resource.Utilization.Platform.entity.Equipment;
import com.project.Lab.Resource.Utilization.Platform.entity.Maintenance;
import com.project.Lab.Resource.Utilization.Platform.repository.BookingRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.EquipmentRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.MaintenanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CsvReportService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    // =========================================================
    // EQUIPMENT CSV
    // =========================================================

    public String exportEquipmentCSV() {

        StringBuilder csv = new StringBuilder();

        csv.append("Equipment ID,Equipment Name,Status,Purchase Date\n");

        for (Equipment equipment : equipmentRepository.findAll()) {

            csv.append(equipment.getEquipmentId()).append(",");

            csv.append(equipment.getEquipmentName()).append(",");

            csv.append(equipment.getStatus()).append(",");

            csv.append(equipment.getPurchaseDate()).append("\n");

        }

        return csv.toString();

    }

    // =========================================================
    // BOOKING CSV
    // =========================================================

    public String exportBookingCSV() {

        StringBuilder csv = new StringBuilder();

        csv.append("Booking ID,Equipment ID,User ID,Status,Start Time,End Time\n");

        for (Booking booking : bookingRepository.findAll()) {

            csv.append(booking.getBookingId()).append(",");

            csv.append(booking.getEquipmentId()).append(",");

            csv.append(booking.getUserId()).append(",");

            csv.append(booking.getStatus()).append(",");

            csv.append(booking.getStartTime()).append(",");

            csv.append(booking.getEndTime()).append("\n");

        }

        return csv.toString();

    }

    // =========================================================
    // MAINTENANCE CSV
    // =========================================================

    public String exportMaintenanceCSV() {

        StringBuilder csv = new StringBuilder();

        csv.append("Work Order,Equipment ID,Status,Priority,Downtime\n");

        for (Maintenance maintenance : maintenanceRepository.findAll()) {

            csv.append(maintenance.getWorkOrderNumber()).append(",");

            csv.append(maintenance.getEquipmentId()).append(",");

            csv.append(maintenance.getStatus()).append(",");

            csv.append(maintenance.getPriority()).append(",");

            csv.append(maintenance.getDowntimeHours()).append("\n");

        }

        return csv.toString();

    }

}