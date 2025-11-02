package com.dss.repository;

import com.dss.dto.DashboardKpiDTO;
import com.dss.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, String> {
    /**
     * * Truy vấn JPQL (Java Persistence Query Language) để tính toán KPI:
     * - SUM(o.totalAmount): Tổng doanh thu (từ bảng Orders)
     * - COUNT(o): Tổng số đơn hàng
     * - COUNT(DISTINCT o.customer): Đếm số lượng Khách hàng (Customer) duy nhất
     * * Kết quả được trả về trực tiếp trong một DTO (Data Transfer Object).
     */
    @Query("SELECT new com.dss.dto.DashboardKpiDTO(" +
            "COALESCE(SUM(o.totalAmount), 0)," +
            " COUNT(o)," +
            " COUNT(DISTINCT o.customer))" +
            " FROM Order o")
    DashboardKpiDTO getKpiData();
}
