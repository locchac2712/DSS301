package com.dss.dto;

import java.math.BigDecimal;

/**
 * TrendDataDTO là class DTO để lưu trữ dữ liệu xu hướng doanh thu.
 * Nó có các phương thức để lấy và đặt dữ liệu.
 * 
 * @author DSS301
 * @version 1.0
 * @since 2025-11-07
 */ 
public class TrendDataDTO {
    private String period; // YYYY-MM, YYYY-Q1, hoặc YYYY
    private BigDecimal totalRevenue; // Tổng doanh thu
    private Long totalOrders; // Tổng số đơn hàng
    private Long totalCustomers; // Tổng số khách hàng
    private BigDecimal predictedRevenue; // Dự báo doanh thu

    public TrendDataDTO() {
    }

    public TrendDataDTO(String period, BigDecimal totalRevenue, Long totalOrders,
            Long totalCustomers, BigDecimal predictedRevenue) {
        this.period = period;
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.totalCustomers = totalCustomers;
        this.predictedRevenue = predictedRevenue;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(Long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public BigDecimal getPredictedRevenue() {
        return predictedRevenue;
    }

    public void setPredictedRevenue(BigDecimal predictedRevenue) {
        this.predictedRevenue = predictedRevenue;
    }
}
