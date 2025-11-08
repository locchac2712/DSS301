package com.dss.repository.mongo;

import com.dss.model.mongo.RetailData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

/**
 * RetailDataRepository là interface MongoRepository để thao tác với collection
 * RetailData trong MongoDB.
 * Nó có các phương thức để thao tác với collection RetailData.
 * 
 * @author DSS301
 * @version 1.0
 * @since 2025-11-07
 */

public interface RetailDataRepository extends MongoRepository<RetailData, String> {

    // Tìm kiếm dữ liệu theo Mã Sản phẩm (StockCode) và Quốc gia (Country)
    List<RetailData> findByStockCodeAndCountry(String stockCode, String country);

    // Tìm kiếm danh sách StockCode duy nhất để hiển thị combobox
    @org.springframework.data.mongodb.repository.Query(value = "{}", fields = "{ 'StockCode' : 1, 'Description' : 1 }")
    List<RetailData> findDistinctStockCodes();

    // Tìm kiếm danh sách Quốc gia duy nhất
    @org.springframework.data.mongodb.repository.Query(value = "{}", fields = "{ 'Country' : 1 }")
    List<RetailData> findDistinctCountries();

    // Tìm kiếm dữ liệu theo Mã Sản phẩm (StockCode)
    Optional<RetailData> findFirstByStockCode(String stockCode);

    // Tìm kiếm dữ liệu theo từ khóa (keyword) và phân trang
    Page<RetailData> findByStockCodeContainingIgnoreCase(String keyword, Pageable pageable);

}