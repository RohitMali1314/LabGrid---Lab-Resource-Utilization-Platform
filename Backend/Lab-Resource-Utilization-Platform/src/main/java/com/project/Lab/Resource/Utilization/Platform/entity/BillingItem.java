package com.project.Lab.Resource.Utilization.Platform.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "billing_items")
public class BillingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "bill_id")
    private Integer billId;

    @Column(name = "equipment_id")
    private Integer equipmentId;

    @Column(name = "description")
    private String description;

    @Column(name = "hours_used")
    private BigDecimal hoursUsed;

    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate;

    @Column(name = "amount")
    private BigDecimal amount;

    public BillingItem() {
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getBillId() {
        return billId;
    }

    public void setBillId(Integer billId) {
        this.billId = billId;
    }

    public Integer getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Integer equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getHoursUsed() {
        return hoursUsed;
    }

    public void setHoursUsed(BigDecimal hoursUsed) {
        this.hoursUsed = hoursUsed;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}