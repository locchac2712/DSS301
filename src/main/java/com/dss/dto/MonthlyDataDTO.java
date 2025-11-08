package com.dss.dto;

public class MonthlyDataDTO {
    private int month;
    private String monthName;
    private int year;
    private double avgQuantity;
    private double totalRevenue;
    private double seasonalityIndex;
    private double forecast;

    public MonthlyDataDTO() {}

    public MonthlyDataDTO(int month, String monthName, int year, double avgQuantity, double totalRevenue) {
        this.month = month;
        this.monthName = monthName;
        this.year = year;
        this.avgQuantity = avgQuantity;
        this.totalRevenue = totalRevenue;
    }

    // Getters and Setters
    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getAvgQuantity() {
        return avgQuantity;
    }

    public void setAvgQuantity(double avgQuantity) {
        this.avgQuantity = avgQuantity;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getSeasonalityIndex() {
        return seasonalityIndex;
    }

    public void setSeasonalityIndex(double seasonalityIndex) {
        this.seasonalityIndex = seasonalityIndex;
    }

    public double getForecast() {
        return forecast;
    }

    public void setForecast(double forecast) {
        this.forecast = forecast;
    }
}

