package com.project.Lab.Resource.Utilization.Platform.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.project.Lab.Resource.Utilization.Platform.entity.Booking;
import com.project.Lab.Resource.Utilization.Platform.entity.Equipment;
import com.project.Lab.Resource.Utilization.Platform.entity.Maintenance;
import com.project.Lab.Resource.Utilization.Platform.repository.BookingRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.EquipmentRepository;
import com.project.Lab.Resource.Utilization.Platform.repository.MaintenanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfReportService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    // =========================================================
    // EQUIPMENT PDF
    // =========================================================

    public byte[] exportEquipmentPdf() throws Exception {

        Document document = new Document();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);

        document.open();

        document.add(new Paragraph("Equipment Report"));

        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(4);

        table.addCell("Equipment ID");
        table.addCell("Equipment");
        table.addCell("Status");
        table.addCell("Purchase Date");

        for (Equipment equipment : equipmentRepository.findAll()) {

            table.addCell(String.valueOf(equipment.getEquipmentId()));
            table.addCell(equipment.getEquipmentName());
            table.addCell(equipment.getStatus());
            table.addCell(String.valueOf(equipment.getPurchaseDate()));
        }

        document.add(table);

        document.close();

        return out.toByteArray();
    }

    // =========================================================
    // BOOKING PDF
    // =========================================================

    public byte[] exportBookingPdf() throws Exception {

        Document document = new Document();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);

        document.open();

        document.add(new Paragraph("Booking Report"));

        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(6);

        table.addCell("Booking ID");
        table.addCell("Equipment");
        table.addCell("User");
        table.addCell("Status");
        table.addCell("Start");
        table.addCell("End");

        for (Booking booking : bookingRepository.findAll()) {

            table.addCell(String.valueOf(booking.getBookingId()));
            table.addCell(String.valueOf(booking.getEquipmentId()));
            table.addCell(String.valueOf(booking.getUserId()));
            table.addCell(booking.getStatus());
            table.addCell(String.valueOf(booking.getStartTime()));
            table.addCell(String.valueOf(booking.getEndTime()));
        }

        document.add(table);

        document.close();

        return out.toByteArray();
    }

    // =========================================================
    // MAINTENANCE PDF
    // =========================================================

    public byte[] exportMaintenancePdf() throws Exception {

        Document document = new Document();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);

        document.open();

        document.add(new Paragraph("Maintenance Report"));

        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(5);

        table.addCell("Work Order");
        table.addCell("Equipment");
        table.addCell("Status");
        table.addCell("Priority");
        table.addCell("Downtime");

        for (Maintenance maintenance : maintenanceRepository.findAll()) {

            table.addCell(maintenance.getWorkOrderNumber());
            table.addCell(String.valueOf(maintenance.getEquipmentId()));
            table.addCell(maintenance.getStatus());
            table.addCell(maintenance.getPriority());
            table.addCell(String.valueOf(maintenance.getDowntimeHours()));
        }

        document.add(table);

        document.close();

        return out.toByteArray();
    }

}