package com.dss.service;

import com.dss.dto.DiscountScenarioDTO;
import com.dss.dto.DssResultDTO;
import com.dss.model.mongo.RetailData;
import com.dss.repository.mongo.RetailDataRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DiscountDssService {

    private final RetailDataRepository retailDataRepository;


    public Optional<RetailData> getDetailsByStockCode(String stockCode) {
        // Sử dụng một phương thức mới mà chúng ta sẽ thêm vào Repository
        return retailDataRepository.findFirstByStockCode(stockCode);
    }

    // Giả định chi phí = 60% giá bán
    private static final double COST_MARGIN = 0.6;

    public DiscountDssService(RetailDataRepository retailDataRepository) {
        this.retailDataRepository = retailDataRepository;
    }

    // Lấy dữ liệu cho các Combobox
    public List<String> getDistinctStockCodes() {
        return retailDataRepository.findDistinctStockCodes().stream()
                .map(RetailData::getStockCode)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getDistinctCountries() {
        return retailDataRepository.findDistinctCountries().stream()
                .map(RetailData::getCountry)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // Hàm chạy mô phỏng
    public DssResultDTO runSimulation(String stockCode, String country, String optimizeOn) {

        // 1. Lấy dữ liệu gốc từ MongoDB
        List<RetailData> data = retailDataRepository.findByStockCodeAndCountry(stockCode, country);

        if (data.isEmpty()) {
            return new DssResultDTO(); // Trả về rỗng nếu không có dữ liệu
        }

        // 2. Tính toán cơ sở (Baseline)
        double totalBaseQuantity = data.stream().mapToInt(RetailData::getQuantity).sum();
        double avgPrice = data.stream().mapToDouble(RetailData::getUnitPrice).average().orElse(0);
        double costPerUnit = avgPrice * COST_MARGIN; // Chi phí giả định

        // 3. Tạo các kịch bản
        List<DiscountScenarioDTO> scenarios = new ArrayList<>();
        int[] discountLevels = {0, 5, 10, 15, 20}; // Các mức giảm giá

        for (int discount : discountLevels) {

            // 4. Mô phỏng (Simulate)
            // Đây là mô phỏng Độ co giãn của Cầu (Price Elasticity) ĐƠN GIẢN
            // Giả định: Cứ giảm 1% giá, doanh số tăng 1.5%
            double elasticityFactor = 1.5;
            double priceModifier = 1.0 - (discount / 100.0);
            double quantityModifier = 1.0 + ((discount / 100.0) * elasticityFactor);

            double simulatedPrice = avgPrice * priceModifier;
            double simulatedQuantity = totalBaseQuantity * quantityModifier;

            // 5. Tính toán Doanh thu và Lợi nhuận
            double expectedRevenue = simulatedPrice * simulatedQuantity;
            double totalCost = costPerUnit * simulatedQuantity;
            double expectedProfit = expectedRevenue - totalCost;

            scenarios.add(new DiscountScenarioDTO(discount, expectedRevenue, expectedProfit));
        }

        // 6. Tìm mức tối ưu
        DiscountScenarioDTO optimal;
        if ("profit".equals(optimizeOn)) {
            optimal = scenarios.stream()
                    .max(Comparator.comparing(DiscountScenarioDTO::getExpectedProfit))
                    .orElse(null);
        } else { // Mặc định là Tối ưu Doanh thu (Revenue)
            optimal = scenarios.stream()
                    .max(Comparator.comparing(DiscountScenarioDTO::getExpectedRevenue))
                    .orElse(null);
        }

        // 7. Tạo kết quả
        DssResultDTO result = new DssResultDTO();
        result.setAllScenarios(scenarios);
        result.setOptimalScenario(optimal);

        // 8. Tạo Phân tích/Gợi ý (Insights)
        if (optimal != null) {
            result.setAnalysis(String.format(
                    "Đã phân tích 5 kịch bản giảm giá cho sản phẩm '%s' tại '%s'.",
                    stockCode, country
            ));
            result.setRecommendation(String.format(
                    "GỢI Ý: Mức giảm giá tối ưu là %d%% (để tối đa hóa %s), dự kiến mang lại %.2f doanh thu và %.2f lợi nhuận.",
                    optimal.getDiscountPercent(), optimizeOn, optimal.getExpectedRevenue(), optimal.getExpectedProfit()
            ));
        }

        return result;
    }

    //    Lấy dữ liệu thô từ MongoDB, hỗ trợ Tìm kiếm theo StockCode
    public Page<RetailData> getMongoDataPaginated(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            // (A) Sửa: Gọi hàm tìm kiếm theo StockCode
            return retailDataRepository.findByStockCodeContainingIgnoreCase(keyword, pageable);
        } else {
            // (B) Nếu không, gọi hàm findAll (phân trang)
            return retailDataRepository.findAll(pageable);
        }
    }
}