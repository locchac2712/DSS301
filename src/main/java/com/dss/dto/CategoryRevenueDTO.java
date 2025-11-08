package com.dss.dto;

import java.math.BigDecimal;

public class CategoryRevenueDTO {

    private String category;
    private double totalRevenue;

    // Constructor
    public CategoryRevenueDTO() {}

    public CategoryRevenueDTO(String category, BigDecimal totalRevenue) {
        this.category = category;
        this.totalRevenue = (totalRevenue != null) ? totalRevenue.doubleValue() : 0.0;
    }

    // Getters and Setter
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public double getTotalRevenue() {
        return totalRevenue;
    }
    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}