package com.project.Lab.Resource.Utilization.Platform.dto;

public class MonthlyHeatmapDTO {

    private Integer month;
    private Integer day;
    private Long bookings;

    public MonthlyHeatmapDTO() {
    }

    public MonthlyHeatmapDTO(
            Integer month,
            Integer day,
            Long bookings
    ) {
        this.month = month;
        this.day = day;
        this.bookings = bookings;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Long getBookings() {
        return bookings;
    }

    public void setBookings(Long bookings) {
        this.bookings = bookings;
    }
}