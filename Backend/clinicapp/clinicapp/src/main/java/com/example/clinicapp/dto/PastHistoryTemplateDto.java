package com.example.clinicapp.dto;
public class PastHistoryTemplateDto {
    private String name;
    private String pastHistory;

    // Constructors
    public PastHistoryTemplateDto() {}
    public PastHistoryTemplateDto(String name, String pastHistory) {
        this.name = name;
        this.pastHistory = pastHistory;
    }

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPastHistory() { return pastHistory; }
    public void setPastHistory(String pastHistory) { this.pastHistory = pastHistory; }
}