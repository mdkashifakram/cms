package com.example.clinicapp.dto;

public class NextVisitDto {
    private String number;
    private String unit;
    private String date;

    public NextVisitDto() {}

    public NextVisitDto(String number, String unit, String date) {
        this.number = number;
        this.unit = unit;
        this.date = date;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}