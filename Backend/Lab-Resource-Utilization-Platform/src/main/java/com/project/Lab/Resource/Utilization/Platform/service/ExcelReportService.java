package com.project.Lab.Resource.Utilization.Platform.service;

import com.project.Lab.Resource.Utilization.Platform.entity.Booking;
import com.project.Lab.Resource.Utilization.Platform.entity.Equipment;
import com.project.Lab.Resource.Utilization.Platform.entity.Maintenance;
import com.project.Lab.Resource.Utilization.Platform.repository.BookingRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.EquipmentRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.MaintenanceRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class ExcelReportService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    // =========================================================
    // EQUIPMENT EXCEL
    // =========================================================

    public byte[] exportEquipmentExcel() throws Exception {

        XSSFWorkbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Equipment");

        Row header = sheet.createRow(0);

        header.createCell(0).setCellValue("Equipment ID");
        header.createCell(1).setCellValue("Equipment Name");
        header.createCell(2).setCellValue("Status");
        header.createCell(3).setCellValue("Purchase Date");

        int rowNo = 1;

        for (Equipment equipment : equipmentRepository.findAll()) {

            Row row = sheet.createRow(rowNo++);

            row.createCell(0).setCellValue(equipment.getEquipmentId());

            row.createCell(1).setCellValue(equipment.getEquipmentName());

            row.createCell(2).setCellValue(equipment.getStatus());

            row.createCell(3).setCellValue(
                    String.valueOf(equipment.getPurchaseDate()));
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        workbook.write(out);

        workbook.close();

        return out.toByteArray();
    }    // =========================================================
    // BOOKING EXCEL
    // =========================================================

    public byte[] exportBookingExcel() throws Exception {

        XSSFWorkbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Bookings");

        Row header = sheet.createRow(0);

        header.createCell(0).setCellValue("Booking ID");
        header.createCell(1).setCellValue("Equipment ID");
        header.createCell(2).setCellValue("User ID");
        header.createCell(3).setCellValue("Status");
        header.createCell(4).setCellValue("Start Time");
        header.createCell(5).setCellValue("End Time");

        int rowNo = 1;

        for (Booking booking : bookingRepository.findAll()) {

            Row row = sheet.createRow(rowNo++);

            row.createCell(0).setCellValue(booking.getBookingId());

            row.createCell(1).setCellValue(booking.getEquipmentId());

            row.createCell(2).setCellValue(booking.getUserId());

            row.createCell(3).setCellValue(booking.getStatus());

            row.createCell(4).setCellValue(
                    String.valueOf(booking.getStartTime()));

            row.createCell(5).setCellValue(
                    String.valueOf(booking.getEndTime()));
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        workbook.write(out);

        workbook.close();

        return out.toByteArray();
    }

    // =========================================================
    // MAINTENANCE EXCEL
    // =========================================================

    public byte[] exportMaintenanceExcel() throws Exception {

        XSSFWorkbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Maintenance");

        Row header = sheet.createRow(0);

        header.createCell(0).setCellValue("Work Order");
        header.createCell(1).setCellValue("Equipment ID");
        header.createCell(2).setCellValue("Status");
        header.createCell(3).setCellValue("Priority");
        header.createCell(4).setCellValue("Downtime");

        int rowNo = 1;

        for (Maintenance maintenance : maintenanceRepository.findAll()) {

            Row row = sheet.createRow(rowNo++);

            row.createCell(0).setCellValue(
                    maintenance.getWorkOrderNumber());

            row.createCell(1).setCellValue(
                    maintenance.getEquipmentId());

            row.createCell(2).setCellValue(
                    maintenance.getStatus());

            row.createCell(3).setCellValue(
                    maintenance.getPriority());

            row.createCell(4).setCellValue(
                    maintenance.getDowntimeHours() == null
                            ? 0
                            : maintenance.getDowntimeHours());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        workbook.write(out);

        workbook.close();

        return out.toByteArray();
    }

}