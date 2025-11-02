package com.dss.service;

import com.dss.model.Product;
import com.dss.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    /**
     * Lấy một TRANG (Page) sản phẩm, hỗ trợ cả lọc và phân trang.
     */
    public Page<Product> getProducts(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            // Nếu có từ khóa, lọc theo tên
            return repo.findByNameContaining(keyword, pageable);
        } else {
            // Nếu không có từ khóa, chỉ phân trang
            return repo.findAll(pageable);
        }
    }

    // READ ALL
    public List<Product> getAll() {
        return repo.findAll();
    }

    // CREATE & UPDATE: Dùng save() để lưu hoặc cập nhật
    public Product save(Product product) {
        return repo.save(product);
    }

    // READ BY ID
    public Optional<Product> getById(String id) {
        return repo.findById(id);
    }

    // DELETE
    public void delete(String id) {
        repo.deleteById(id);
    }
}