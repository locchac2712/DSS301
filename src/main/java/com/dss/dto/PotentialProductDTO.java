package com.dss.dto;

/**
 * PotentialProductDTO là class DTO để lưu trữ dữ liệu sản phẩm tiềm năng.
 * Nó có các phương thức để lấy và đặt dữ liệu.
 * 
 * @author DSS301
 * @version 1.0
 * @since 2025-11-07
 */
public class PotentialProductDTO {
    private String stockCode; // Mã sản phẩm
    private String description; // Mô tả sản phẩm
    private Double potentialScore; // Tỷ lệ tiềm năng (%)

    public PotentialProductDTO() {
    }

    public PotentialProductDTO(String stockCode, String description, Double potentialScore) {
        this.stockCode = stockCode;
        this.description = description;
        this.potentialScore = potentialScore;
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

    public Double getPotentialScore() {
        return potentialScore;
    }

    public void setPotentialScore(Double potentialScore) {
        this.potentialScore = potentialScore;
    }
}
