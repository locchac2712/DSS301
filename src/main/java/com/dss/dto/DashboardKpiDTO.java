package com.dss.dto;

import java.math.BigDecimal; // Dùng BigDecimal cho tính toán tài chính an toàn

public class DashboardKpiDTO {

    private double totalRevenue; // Tổng doanh thu
    private long totalOrders;    // Tổng số đơn hàng
    private double avgOrderValue;  // Giá trị đơn hàng trung bình
    private long totalCustomers; // Tổng số khách hàng (duy nhất)

    // Constructor rỗng (Bắt buộc)
    public DashboardKpiDTO() {
    }

    // Constructor này được gọi bởi truy vấn @Query trong OrderRepository
    public DashboardKpiDTO(BigDecimal totalRevenue, Long totalOrders, Long totalCustomers) {
        this.totalRevenue = (totalRevenue != null) ? totalRevenue.doubleValue() : 0.0;
        this.totalOrders = (totalOrders != null) ? totalOrders : 0L;
        this.totalCustomers = (totalCustomers != null) ? totalCustomers : 0L;

        // Tự động tính AOV (Giá trị Đơn hàng Trung bình)
        if (this.totalOrders > 0) {
            // totalRevenue đã là BigDecimal, không cần chuyển đổi
            BigDecimal orders = BigDecimal.valueOf(this.totalOrders);
            this.avgOrderValue = totalRevenue.divide(orders, 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        } else {
            this.avgOrderValue = 0.0;
        }
    }

    // --- Getters and Setters (Bắt buộc) ---

    public double getTotalRevenue() {
        return totalRevenue;
    }
    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    public long getTotalOrders() {
        return totalOrders;
    }
    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }
    public double getAvgOrderValue() {
        return avgOrderValue;
    }
    public void setAvgOrderValue(double avgOrderValue) {
        this.avgOrderValue = avgOrderValue;
    }
    public long getTotalCustomers() {
        return totalCustomers;
    }
    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }
}