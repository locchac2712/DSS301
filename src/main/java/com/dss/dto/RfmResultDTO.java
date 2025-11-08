package com.dss.dto;

import java.util.List;

/**
 * RfmResultDTO là class DTO để lưu trữ kết quả phân tích RFM.
 * Nó có các phương thức để lấy và đặt dữ liệu.
 * 
 * @author DSS301
 * @version 1.0
 * @since 2025-11-07
 */
public class RfmResultDTO {
    private String selectedSegment; // Segment được chọn
    private String timePeriod; // Thời gian được chọn
    private List<PotentialProductDTO> potentialProducts; // Sản phẩm tiềm năng
    private List<PopularProductDTO> popularProducts; // Sản phẩm phổ biến
    private List<TrendDataDTO> trendData; // Dữ liệu xu hướng doanh thu
    private Double avgRecencyScore; // Điểm RFM trung bình
    private Double avgFrequencyScore; // Điểm RFM trung bình
    private Double avgMonetaryScore; // Điểm RFM trung bình

    public RfmResultDTO() {
    }

    public String getSelectedSegment() {
        return selectedSegment;
    }

    public void setSelectedSegment(String selectedSegment) {
        this.selectedSegment = selectedSegment;
    }

    public String getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(String timePeriod) {
        this.timePeriod = timePeriod;
    }

    public List<PotentialProductDTO> getPotentialProducts() {
        return potentialProducts;
    }

    public void setPotentialProducts(List<PotentialProductDTO> potentialProducts) {
        this.potentialProducts = potentialProducts;
    }

    public List<PopularProductDTO> getPopularProducts() {
        return popularProducts;
    }

    public void setPopularProducts(List<PopularProductDTO> popularProducts) {
        this.popularProducts = popularProducts;
    }

    public List<TrendDataDTO> getTrendData() {
        return trendData;
    }

    public void setTrendData(List<TrendDataDTO> trendData) {
        this.trendData = trendData;
    }

    public Double getAvgRecencyScore() {
        return avgRecencyScore;
    }

    public void setAvgRecencyScore(Double avgRecencyScore) {
        this.avgRecencyScore = avgRecencyScore;
    }

    public Double getAvgFrequencyScore() {
        return avgFrequencyScore;
    }

    public void setAvgFrequencyScore(Double avgFrequencyScore) {
        this.avgFrequencyScore = avgFrequencyScore;
    }

    public Double getAvgMonetaryScore() {
        return avgMonetaryScore;
    }

    public void setAvgMonetaryScore(Double avgMonetaryScore) {
        this.avgMonetaryScore = avgMonetaryScore;
    }
}
