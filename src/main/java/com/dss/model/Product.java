package com.dss.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Objects; // Import Objects

@Entity
@Table(name = "products")
public class Product {

    @Id
    @Column(name = "stock_code")
    @NotBlank(message = "Stock Code không được để trống")
    private String id;

    @Column(name = "description")
    @NotBlank(message = "Tên sản phẩm không được để trống") // (3)
    private String name;

    @NotBlank(message = "Danh mục không được để trống")
    private String category;

    @Column(name = "unit_price")
    @NotNull(message = "Giá không được để trống") // (5) Dùng @NotNull cho số
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    @Column(name = "stock_quantity")
    @Min(value = 0, message = "Số lượng không được âm")
    private int stockQuantity;

    // 1. Constructor rỗng (Bắt buộc cho JPA)
    public Product() {
    }

    // 2. Getters and Setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public int getStockQuantity() {
        return stockQuantity;
    }
    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    // 3. equals() và hashCode() (Quan trọng cho JPA)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}