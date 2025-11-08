-- =======================================================
-- PostgreSQL Database Schema và Dữ liệu Mẫu
-- =======================================================

-- =======================================================
-- 1. Xóa các bảng cũ nếu tồn tại (theo thứ tự phụ thuộc)
-- =======================================================
DROP TABLE IF EXISTS revenue_trends CASCADE;
DROP TABLE IF EXISTS demand_forecasts CASCADE;
DROP TABLE IF EXISTS campaign_proposals CASCADE;
DROP TABLE IF EXISTS customer_predictions CASCADE;
DROP TABLE IF EXISTS sales_summary CASCADE;
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS customers CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- =======================================================
-- 2. Tạo các bảng Giao dịch (OLTP) - Dùng cho ứng dụng chính
-- =======================================================

-- Bảng Customers (Khách hàng)
CREATE TABLE IF NOT EXISTS customers (
    customer_id VARCHAR(20) PRIMARY KEY,
    customer_name VARCHAR(100),
    country VARCHAR(100),
    region VARCHAR(100),
    email VARCHAR(100),
    gender VARCHAR(10),
    age INTEGER
);

-- Bảng Products (Sản phẩm)
CREATE TABLE IF NOT EXISTS products (
    stock_code VARCHAR(20) PRIMARY KEY,
    description VARCHAR(255), -- Ánh xạ tới trường 'name' trong Java
    category VARCHAR(100),
    unit_price NUMERIC(10, 2), -- Ánh xạ tới trường 'price' trong Java (BigDecimal)
    stock_quantity INTEGER DEFAULT 0 -- (QUAN TRỌNG: Đã thêm cột này)
);

-- Bảng Users (Người dùng hệ thống - Đăng nhập)
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng Orders (Đơn hàng - Header)
CREATE TABLE IF NOT EXISTS orders (
    order_id VARCHAR(20) PRIMARY KEY,
    customer_id VARCHAR(20) NOT NULL,
    order_date DATE NOT NULL,
    status VARCHAR(50) DEFAULT 'Pending',
    subtotal NUMERIC(10, 2) DEFAULT 0,
    tax NUMERIC(10, 2) DEFAULT 0,
    shipping_fee NUMERIC(10, 2) DEFAULT 0,
    total_amount NUMERIC(10, 2) DEFAULT 0, -- Tổng doanh thu
    payment_method VARCHAR(50),
    shipping_address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- Bảng Order Items (Chi tiết đơn hàng)
CREATE TABLE IF NOT EXISTS order_items (
    item_id SERIAL PRIMARY KEY,
    order_id VARCHAR(20) NOT NULL,
    product_id VARCHAR(20) NOT NULL, -- Liên kết với stock_code
    quantity INTEGER DEFAULT 1,
    unit_price NUMERIC(10, 2) DEFAULT 0, -- Giá tại thời điểm mua
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(stock_code)
);

-- =======================================================
-- 3. Tạo các bảng Phân tích (OLAP) - Dùng cho báo cáo nâng cao
-- =======================================================

-- Bảng Tóm tắt Bán hàng
CREATE TABLE IF NOT EXISTS sales_summary (
    id SERIAL PRIMARY KEY,
    country VARCHAR(100),
    stock_code VARCHAR(20),
    total_quantity INTEGER,
    total_revenue NUMERIC(12, 2),
    ranking INTEGER,
    analysis_date DATE,
    CONSTRAINT fk_sales_summary_product FOREIGN KEY (stock_code) REFERENCES products(stock_code)
);

-- Bảng Dự đoán Khách hàng (Churn/RFM)
CREATE TABLE IF NOT EXISTS customer_predictions (
    id SERIAL PRIMARY KEY,
    customer_id VARCHAR(20),
    recency INTEGER,
    frequency INTEGER,
    monetary NUMERIC(12, 2),
    repurchase_probability NUMERIC(5, 2),
    prediction_date DATE,
    segment VARCHAR(50),
    CONSTRAINT fk_customer_predictions_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- Bảng Đề xuất Chiến dịch Marketing
CREATE TABLE IF NOT EXISTS campaign_proposals (
    campaign_id SERIAL PRIMARY KEY,
    campaign_name VARCHAR(100),
    target_segment VARCHAR(50),
    expected_cost NUMERIC(12, 2),
    expected_revenue NUMERIC(12, 2),
    roi NUMERIC(5, 2),
    created_at DATE
);

-- Bảng Dự báo Nhu cầu (Inventory Forecasting)
CREATE TABLE IF NOT EXISTS demand_forecasts (
    forecast_id SERIAL PRIMARY KEY,
    category VARCHAR(100),
    forecast_date DATE,
    actual_sales NUMERIC(12, 2),
    predicted_sales NUMERIC(12, 2),
    forecast_model VARCHAR(50)
);

-- Bảng Xu hướng Doanh thu
CREATE TABLE IF NOT EXISTS revenue_trends (
    id SERIAL PRIMARY KEY,
    period VARCHAR(20),
    total_revenue NUMERIC(12, 2),
    growth_rate NUMERIC(5, 2),
    recorded_at DATE
);

-- =======================================================
-- 4. Xóa dữ liệu cũ (để tránh lỗi trùng lặp khi chạy lại)
-- =======================================================
TRUNCATE TABLE order_items RESTART IDENTITY CASCADE;
TRUNCATE TABLE orders RESTART IDENTITY CASCADE;
TRUNCATE TABLE sales_summary RESTART IDENTITY CASCADE;
TRUNCATE TABLE customer_predictions RESTART IDENTITY CASCADE;
TRUNCATE TABLE campaign_proposals RESTART IDENTITY CASCADE;
TRUNCATE TABLE demand_forecasts RESTART IDENTITY CASCADE;
TRUNCATE TABLE revenue_trends RESTART IDENTITY CASCADE;
TRUNCATE TABLE products RESTART IDENTITY CASCADE;
TRUNCATE TABLE customers RESTART IDENTITY CASCADE;
TRUNCATE TABLE users RESTART IDENTITY CASCADE;

-- =======================================================
-- 5. Chèn Dữ liệu Mới
-- =======================================================

-- (A) Bảng Users
INSERT INTO users (username, password_hash, role) 
VALUES 
('manager', 'pass123', 'MANAGER'),
('admin', 'adminpass', 'ADMIN')
ON CONFLICT (username) DO NOTHING;

-- (B) Bảng Customers
INSERT INTO customers (customer_id, customer_name, country, region, email, gender, age) 
VALUES
('C-001', 'Alice Johnson', 'USA', 'North America', 'alice@example.com', 'Female', 28),
('C-002', 'Bob Smith', 'Canada', 'North America', 'bob@example.com', 'Male', 34),
('C-003', 'Charly Garcia', 'Mexico', 'North America', 'charly@example.com', 'Male', 45),
('C-004', 'David Kim', 'South Korea', 'Asia', 'david@example.com', 'Male', 22),
('C-005', 'Eva Muller', 'Germany', 'Europe', 'eva@example.com', 'Female', 51)
ON CONFLICT (customer_id) DO NOTHING;

-- (C) Bảng Products (Có tồn kho để test Low Stock)
INSERT INTO products (stock_code, description, category, unit_price, stock_quantity) 
VALUES
('P-001', 'iPhone 15 Pro 256GB', 'Electronics', 999.99, 50),
('P-002', 'Dell XPS 15 Laptop', 'Laptop', 1599.00, 5),   -- Low Stock (<10)
('P-003', 'Sony WH-1000XM5 Headphones', 'Headphones', 349.99, 100),
('P-004', 'Apple Watch Ultra 2', 'Wearable', 799.00, 75),
('P-005', 'Samsung QLED 4K 65-inch TV', 'Electronics', 1499.99, 2), -- Low Stock (<10)
('P-006', 'Logitech MX Master 3S Mouse', 'Accessories', 99.99, 150),
('P-007', 'MacBook Air M3', 'Laptop', 1299.00, 8),    -- Low Stock (<10)
('P-008', 'Bose QuietComfort Earbuds II', 'Headphones', 299.00, 80),
('P-009', 'GoPro HERO12 Black', 'Camera', 399.99, 60),
('P-010', 'Kindle Paperwhite', 'Electronics', 139.99, 120)
ON CONFLICT (stock_code) DO NOTHING;

-- (D) Bảng Orders & Order_Items (Dữ liệu giao dịch)
-- Đơn hàng 1 (Tháng 10)
INSERT INTO orders (order_id, customer_id, order_date, total_amount, status) 
VALUES ('O-1001', 'C-001', '2025-10-20', 1349.98, 'Completed')
ON CONFLICT (order_id) DO NOTHING;
INSERT INTO order_items (order_id, product_id, quantity, unit_price) 
VALUES ('O-1001', 'P-001', 1, 999.99), ('O-1001', 'P-003', 1, 349.99);

-- Đơn hàng 2 (Tháng 10)
INSERT INTO orders (order_id, customer_id, order_date, total_amount, status) 
VALUES ('O-1002', 'C-002', '2025-10-22', 3198.00, 'Completed')
ON CONFLICT (order_id) DO NOTHING;
INSERT INTO order_items (order_id, product_id, quantity, unit_price) 
VALUES ('O-1002', 'P-002', 2, 1599.00);

-- Đơn hàng 3 (Tháng 10)
INSERT INTO orders (order_id, customer_id, order_date, total_amount, status) 
VALUES ('O-1003', 'C-001', '2025-10-25', 1148.99, 'Shipped')
ON CONFLICT (order_id) DO NOTHING;
INSERT INTO order_items (order_id, product_id, quantity, unit_price) 
VALUES ('O-1003', 'P-004', 1, 799.00), ('O-1003', 'P-003', 1, 349.99);

-- Đơn hàng 4 (Tháng 10)
INSERT INTO orders (order_id, customer_id, order_date, total_amount, status) 
VALUES ('O-1004', 'C-003', '2025-10-28', 199.98, 'Pending')
ON CONFLICT (order_id) DO NOTHING;
INSERT INTO order_items (order_id, product_id, quantity, unit_price) 
VALUES ('O-1004', 'P-006', 2, 99.99);

-- Đơn hàng 5 (Tháng 10)
INSERT INTO orders (order_id, customer_id, order_date, total_amount, status) 
VALUES ('O-1005', 'C-004', '2025-10-29', 1698.99, 'Completed')
ON CONFLICT (order_id) DO NOTHING;
INSERT INTO order_items (order_id, product_id, quantity, unit_price) 
VALUES ('O-1005', 'P-007', 1, 1299.00), ('O-1005', 'P-009', 1, 399.99);

-- Đơn hàng 6 (Tháng 10)
INSERT INTO orders (order_id, customer_id, order_date, total_amount, status) 
VALUES ('O-1006', 'C-005', '2025-10-30', 1499.99, 'Shipped')
ON CONFLICT (order_id) DO NOTHING;
INSERT INTO order_items (order_id, product_id, quantity, unit_price) 
VALUES ('O-1006', 'P-005', 1, 1499.99);

-- Đơn hàng 7 (Tháng 10)
INSERT INTO orders (order_id, customer_id, order_date, total_amount, status) 
VALUES ('O-1007', 'C-002', '2025-10-31', 439.98, 'Completed')
ON CONFLICT (order_id) DO NOTHING;
INSERT INTO order_items (order_id, product_id, quantity, unit_price) 
VALUES ('O-1007', 'P-008', 1, 299.00), ('O-1007', 'P-010', 1, 139.99);

-- Đơn hàng 8 (Tháng 11 - Mới nhất)
INSERT INTO orders (order_id, customer_id, order_date, total_amount, status) 
VALUES ('O-1008', 'C-001', '2025-11-01', 99.99, 'Pending')
ON CONFLICT (order_id) DO NOTHING;
INSERT INTO order_items (order_id, product_id, quantity, unit_price) 
VALUES ('O-1008', 'P-006', 1, 99.99);
