package com.dss.dto;

// DTO này chứa kết quả cho 1 kịch bản
public class DiscountScenarioDTO {
    private int discountPercent;
    private double expectedRevenue;
    private double expectedProfit;

    public DiscountScenarioDTO(int discountPercent, double expectedRevenue, double expectedProfit) {
        this.discountPercent = discountPercent;
        this.expectedRevenue = expectedRevenue;
        this.expectedProfit = expectedProfit;
    }


    // getters and setters
    public int getDiscountPercent() {
        return discountPercent;
    }
    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }
    public double getExpectedRevenue() {
        return expectedRevenue;
    }
    public void setExpectedRevenue(double expectedRevenue) {
        this.expectedRevenue = expectedRevenue;
    }
    public double getExpectedProfit() {
        return expectedProfit;
    }
    public void setExpectedProfit(double expectedProfit) {
        this.expectedProfit = expectedProfit;
    }

}