package com.dss.dto;

import java.util.List;

public class SeasonalForecastResultDTO {
    private String stockCode;
    private String productName;
    private String country;
    private List<MonthlyDataDTO> historicalData;
    private List<MonthlyDataDTO> forecastData;
    private double overallSeasonalityScore;
    private String seasonalityLevel; // HIGH, MEDIUM, LOW
    private String recommendation;
    private String analysis;

    public SeasonalForecastResultDTO() {}

    // Getters and Setters
    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<MonthlyDataDTO> getHistoricalData() {
        return historicalData;
    }

    public void setHistoricalData(List<MonthlyDataDTO> historicalData) {
        this.historicalData = historicalData;
    }

    public List<MonthlyDataDTO> getForecastData() {
        return forecastData;
    }

    public void setForecastData(List<MonthlyDataDTO> forecastData) {
        this.forecastData = forecastData;
    }

    public double getOverallSeasonalityScore() {
        return overallSeasonalityScore;
    }

    public void setOverallSeasonalityScore(double overallSeasonalityScore) {
        this.overallSeasonalityScore = overallSeasonalityScore;
    }

    public String getSeasonalityLevel() {
        return seasonalityLevel;
    }

    public void setSeasonalityLevel(String seasonalityLevel) {
        this.seasonalityLevel = seasonalityLevel;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }
}

