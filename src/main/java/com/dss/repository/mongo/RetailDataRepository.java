package com.dss.repository.mongo;

import com.dss.model.mongo.RetailData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

// (MongoRepository dùng String làm ID mặc định)
public interface RetailDataRepository extends MongoRepository<RetailData, String> {

    // Lấy tất cả dữ liệu theo Mã Sản phẩm (StockCode) và Quốc gia (Country)
    List<RetailData> findByStockCodeAndCountry(String stockCode, String country);

    // Lấy danh sách StockCode duy nhất để hiển thị combobox
    @org.springframework.data.mongodb.repository.Query(value = "{}", fields = "{ 'StockCode' : 1, 'Description' : 1 }")
    List<RetailData> findDistinctStockCodes();

    // Lấy danh sách Quốc gia duy nhất
    @org.springframework.data.mongodb.repository.Query(value = "{}", fields = "{ 'Country' : 1 }")
    List<RetailData> findDistinctCountries();

    Optional<RetailData> findFirstByStockCode(String stockCode);

    Page<RetailData> findByStockCodeContainingIgnoreCase(String keyword, Pageable pageable);
}