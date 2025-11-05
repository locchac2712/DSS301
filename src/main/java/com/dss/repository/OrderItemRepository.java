package com.dss.repository;

import com.dss.dto.CategoryRevenueDTO;
import com.dss.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    /**
     * Truy vấn JPQL để lấy doanh thu theo danh mục từ PostgreSQL:
     * 1JOIN (Kết nối) OrderItem (oi) với Product (p)
     * 2GROUP BY (Gom nhóm) theo Danh mục (p.category)
     * 3SUM (Tính tổng) của (quantity * unitPrice)
     */
    @Query("SELECT new com.dss.dto.CategoryRevenueDTO(p.category, SUM(oi.quantity * oi.unitPrice)) " +
            "FROM OrderItem oi JOIN oi.product p " +
            "GROUP BY p.category " +
            "ORDER BY SUM(oi.quantity * oi.unitPrice) DESC")
    List<CategoryRevenueDTO> getRevenueByCategoryFromPostgres();

}