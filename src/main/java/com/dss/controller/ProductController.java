package com.dss.controller;

import com.dss.model.Product;
import com.dss.service.ProductService;
import org.springframework.http.ResponseEntity; // Cần import ResponseEntity
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }


    @GetMapping("/by-stockcode/{stockCode}")
    public ResponseEntity<Product> getProductByStockCode(@PathVariable String stockCode) {
        return service.getById(stockCode)
                .map(ResponseEntity::ok)        // Nếu tìm thấy, trả về 200 OK + Product (JSON)
                .orElseGet(() -> ResponseEntity.notFound().build()); // Không tìm thấy, trả về 404
    }


    // CREATE (Thêm mới) - POST /api/products
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return service.save(product);
    }

    // READ (Lấy tất cả) - GET /api/products
    @GetMapping
    public List<Product> getAllProducts() {
        return service.getAll();
    }

    // READ (Lấy theo ID) - GET /api/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        return service.getById(id)
                .map(ResponseEntity::ok)        // Nếu tìm thấy, trả về 200 OK
                .orElseGet(() -> ResponseEntity.notFound().build()); // Không tìm thấy, trả về 404 Not Found
    }

    // UPDATE (Sửa đổi) - PUT /api/products/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody Product productDetails) {
        return service.getById(id)
                .map(existingProduct -> {
                    // Cập nhật các trường
                    existingProduct.setName(productDetails.getName());
                    existingProduct.setPrice(productDetails.getPrice());
                    existingProduct.setCategory(productDetails.getCategory());
                    existingProduct.setStockQuantity(productDetails.getStockQuantity());

                    Product updatedProduct = service.save(existingProduct);
                    return ResponseEntity.ok(updatedProduct);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE (Xóa) - DELETE /api/products/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        return service.getById(id)
                .map(product -> {
                    service.delete(id);
                    return ResponseEntity.ok().<Void>build(); // Xóa thành công, trả về 200 OK
                })
                .orElseGet(() -> ResponseEntity.notFound().build()); // Không tìm thấy, trả về 404 Not Found
    }


}