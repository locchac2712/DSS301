package com.dss.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO classes used by the Decision Support endpoints
 */
public class DecisionSupportDTOs {

    // --- Discount result DTO ---
    public static class DiscountChartPoint {
        private int discountPercent;
        private double quantity;
        private BigDecimal revenue;
        private BigDecimal profit;

        public DiscountChartPoint() {}
        public DiscountChartPoint(int discountPercent, double quantity, BigDecimal revenue, BigDecimal profit) {
            this.discountPercent = discountPercent;
            this.quantity = quantity;
            this.revenue = revenue;
            this.profit = profit;
        }

        public int getDiscountPercent() { return discountPercent; }
        public void setDiscountPercent(int discountPercent) { this.discountPercent = discountPercent; }
        public double getQuantity() { return quantity; }
        public void setQuantity(double quantity) { this.quantity = quantity; }
        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
        public BigDecimal getProfit() { return profit; }
        public void setProfit(BigDecimal profit) { this.profit = profit; }
    }

    public static class DiscountResultDTO {
        private String stockCode;
        private String productName;
        private double avgMonthlySales;
        private BigDecimal basePrice;
        private BigDecimal costPerUnit;
        private int forecastMonths;
        private List<DiscountChartPoint> chart;
        private int optimalDiscountPercent;
        private BigDecimal optimalRevenue;
        private BigDecimal optimalProfit;
        private double optimalQuantity;

        public DiscountResultDTO() {}

        public String getStockCode() { return stockCode; }
        public void setStockCode(String stockCode) { this.stockCode = stockCode; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public double getAvgMonthlySales() { return avgMonthlySales; }
        public void setAvgMonthlySales(double avgMonthlySales) { this.avgMonthlySales = avgMonthlySales; }
        public BigDecimal getBasePrice() { return basePrice; }
        public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
        public BigDecimal getCostPerUnit() { return costPerUnit; }
        public void setCostPerUnit(BigDecimal costPerUnit) { this.costPerUnit = costPerUnit; }
        public int getForecastMonths() { return forecastMonths; }
        public void setForecastMonths(int forecastMonths) { this.forecastMonths = forecastMonths; }
        public List<DiscountChartPoint> getChart() { return chart; }
        public void setChart(List<DiscountChartPoint> chart) { this.chart = chart; }
        public int getOptimalDiscountPercent() { return optimalDiscountPercent; }
        public void setOptimalDiscountPercent(int optimalDiscountPercent) { this.optimalDiscountPercent = optimalDiscountPercent; }
        public BigDecimal getOptimalRevenue() { return optimalRevenue; }
        public void setOptimalRevenue(BigDecimal optimalRevenue) { this.optimalRevenue = optimalRevenue; }
        public BigDecimal getOptimalProfit() { return optimalProfit; }
        public void setOptimalProfit(BigDecimal optimalProfit) { this.optimalProfit = optimalProfit; }
        public double getOptimalQuantity() { return optimalQuantity; }
        public void setOptimalQuantity(double optimalQuantity) { this.optimalQuantity = optimalQuantity; }
    }

    // --- Reorder suggestion DTO ---
    public static class ReorderSuggestionDTO {
        private String stockCode;
        private String productName;
        private double avgMonthlySales;
        private int currentStockEstimate;
        private int forecastDemand;
        private int reorderQuantity;

        public ReorderSuggestionDTO() {}

        public String getStockCode() { return stockCode; }
        public void setStockCode(String stockCode) { this.stockCode = stockCode; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public double getAvgMonthlySales() { return avgMonthlySales; }
        public void setAvgMonthlySales(double avgMonthlySales) { this.avgMonthlySales = avgMonthlySales; }
        public int getCurrentStockEstimate() { return currentStockEstimate; }
        public void setCurrentStockEstimate(int currentStockEstimate) { this.currentStockEstimate = currentStockEstimate; }
        public int getForecastDemand() { return forecastDemand; }
        public void setForecastDemand(int forecastDemand) { this.forecastDemand = forecastDemand; }
        public int getReorderQuantity() { return reorderQuantity; }
        public void setReorderQuantity(int reorderQuantity) { this.reorderQuantity = reorderQuantity; }
    }
}
