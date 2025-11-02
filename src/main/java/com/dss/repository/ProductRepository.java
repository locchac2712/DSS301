package com.dss.repository;

import com.dss.model.Product;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.Pageable; // Import Pageable
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {

    /**
     * Tìm kiếm sản phẩm có tên chứa (LIKE %keyword%)
     * và trả về kết quả đã được phân trang.
     */


    Page<Product> findByNameContaining(String keyword, Pageable pageable);
}