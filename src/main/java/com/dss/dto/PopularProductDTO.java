package com.dss.dto;

import java.math.BigDecimal;

/**
 * PopularProductDTO là class DTO để lưu trữ dữ liệu sản phẩm phổ biến.
 * Nó có các phương thức để lấy và đặt dữ liệu.
 * 
 * @author DSS301
 * @version 1.0
 * @since 2025-11-07
 */
public class PopularProductDTO {
    private String stockCode; // Mã sản phẩm
    private String description; // Mô tả sản phẩm
    private Long purchaseCount; // Số lượng mua
    private BigDecimal totalRevenue; // Tổng doanh thu
    private Double purchaseRate; // Tỷ lệ mua (%)

    public PopularProductDTO() {
    }

    public PopularProductDTO(String stockCode, String description, Long purchaseCount,
            BigDecimal totalRevenue, Double purchaseRate) {
        this.stockCode = stockCode;
        this.description = description;
        this.purchaseCount = purchaseCount;
        this.totalRevenue = totalRevenue;
        this.purchaseRate = purchaseRate;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getPurchaseCount() {
        return purchaseCount;
    }

    public void setPurchaseCount(Long purchaseCount) {
        this.purchaseCount = purchaseCount;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Double getPurchaseRate() {
        return purchaseRate;
    }

    public void setPurchaseRate(Double purchaseRate) {
        this.purchaseRate = purchaseRate;
    }
}
