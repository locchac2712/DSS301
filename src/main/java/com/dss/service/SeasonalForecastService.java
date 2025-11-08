package com.dss.service;

import com.dss.dto.MonthlyDataDTO;
import com.dss.dto.SeasonalForecastResultDTO;
import com.dss.model.mongo.RetailData;
import com.dss.repository.mongo.RetailDataRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeasonalForecastService {

    private final RetailDataRepository retailDataRepository;

    public SeasonalForecastService(RetailDataRepository retailDataRepository) {
        this.retailDataRepository = retailDataRepository;
    }

    // Lấy danh sách StockCode và Country cho combobox
    public List<String> getDistinctStockCodes() {
        return retailDataRepository.findDistinctStockCodes().stream()
                .map(RetailData::getStockCode)
                .distinct()
                .sorted()
                .limit(100) // Giới hạn để không quá nhiều
                .collect(Collectors.toList());
    }

    public List<String> getDistinctCountries() {
        return retailDataRepository.findDistinctCountries().stream()
                .map(RetailData::getCountry)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * HÀM CHÍNH: Phân tích và dự báo theo mùa
     */
    public SeasonalForecastResultDTO analyzeSeasonalDemand(String stockCode, String country, int forecastMonths) {

        // 1. Lấy dữ liệu lịch sử
        List<RetailData> data = retailDataRepository.findByStockCodeAndCountry(stockCode, country);

        if (data.isEmpty()) {
            return new SeasonalForecastResultDTO(); // Trả về rỗng nếu không có dữ liệu
        }

        SeasonalForecastResultDTO result = new SeasonalForecastResultDTO();
        result.setStockCode(stockCode);
        result.setProductName(data.get(0).getDescription());
        result.setCountry(country);

        // 2. Nhóm dữ liệu theo tháng
        Map<Integer, List<RetailData>> groupedByMonth = data.stream()
                .filter(d -> d.getInvoiceDate() != null)
                .collect(Collectors.groupingBy(d -> d.getInvoiceDate().getMonthValue()));

        // 3. Tính toán dữ liệu lịch sử theo tháng
        List<MonthlyDataDTO> historicalData = new ArrayList<>();
        double totalYearlyAvg = 0;
        int monthCount = 0;

        for (int month = 1; month <= 12; month++) {
            List<RetailData> monthData = groupedByMonth.getOrDefault(month, new ArrayList<>());
            
            if (!monthData.isEmpty()) {
                double avgQuantity = monthData.stream()
                        .mapToDouble(RetailData::getQuantity)
                        .average()
                        .orElse(0);

                double totalRevenue = monthData.stream()
                        .mapToDouble(d -> d.getUnitPrice() * d.getQuantity())
                        .sum();

                MonthlyDataDTO monthDTO = new MonthlyDataDTO(
                        month,
                        Month.of(month).name(),
                        LocalDateTime.now().getYear(),
                        avgQuantity,
                        totalRevenue
                );

                historicalData.add(monthDTO);
                totalYearlyAvg += avgQuantity;
                monthCount++;
            }
        }

        // 4. Tính chỉ số mùa vụ (Seasonality Index)
        double yearlyAvg = monthCount > 0 ? totalYearlyAvg / monthCount : 1;

        for (MonthlyDataDTO monthDTO : historicalData) {
            double seasonalityIndex = monthDTO.getAvgQuantity() / yearlyAvg;
            monthDTO.setSeasonalityIndex(seasonalityIndex);
        }

        result.setHistoricalData(historicalData);

        // 5. Dự báo cho N tháng tới
        List<MonthlyDataDTO> forecastData = new ArrayList<>();
        int currentMonth = LocalDateTime.now().getMonthValue();

        // Sử dụng Simple Moving Average (3 tháng gần nhất)
        double movingAvg = historicalData.stream()
                .sorted(Comparator.comparing(MonthlyDataDTO::getMonth).reversed())
                .limit(3)
                .mapToDouble(MonthlyDataDTO::getAvgQuantity)
                .average()
                .orElse(yearlyAvg);

        for (int i = 1; i <= forecastMonths; i++) {
            int forecastMonth = (currentMonth + i - 1) % 12 + 1;
            
            // Tìm seasonality index của tháng này từ historical data
            double seasonalityIndex = historicalData.stream()
                    .filter(h -> h.getMonth() == forecastMonth)
                    .findFirst()
                    .map(MonthlyDataDTO::getSeasonalityIndex)
                    .orElse(1.0);

            // Dự báo = Moving Average × Seasonality Index
            double forecast = movingAvg * seasonalityIndex;

            MonthlyDataDTO forecastDTO = new MonthlyDataDTO();
            forecastDTO.setMonth(forecastMonth);
            forecastDTO.setMonthName(Month.of(forecastMonth).name());
            forecastDTO.setYear(LocalDateTime.now().getYear());
            forecastDTO.setForecast(forecast);
            forecastDTO.setSeasonalityIndex(seasonalityIndex);

            forecastData.add(forecastDTO);
        }

        result.setForecastData(forecastData);

        // 6. Tính Overall Seasonality Score
        double maxIndex = historicalData.stream()
                .mapToDouble(MonthlyDataDTO::getSeasonalityIndex)
                .max()
                .orElse(1.0);
        double minIndex = historicalData.stream()
                .mapToDouble(MonthlyDataDTO::getSeasonalityIndex)
                .min()
                .orElse(1.0);

        double seasonalityScore = maxIndex - minIndex;
        result.setOverallSeasonalityScore(seasonalityScore);

        // 7. Phân loại mức độ mùa vụ
        if (seasonalityScore > 1.0) {
            result.setSeasonalityLevel("HIGH");
        } else if (seasonalityScore > 0.5) {
            result.setSeasonalityLevel("MEDIUM");
        } else {
            result.setSeasonalityLevel("LOW");
        }

        // 8. Tạo Analysis và Recommendation
        result.setAnalysis(String.format(
                "Phân tích %d tháng dữ liệu lịch sử cho sản phẩm '%s' tại '%s'. " +
                "Chỉ số mùa vụ: %.2f (%s). Trung bình bán: %.0f sản phẩm/tháng.",
                historicalData.size(),
                result.getProductName(),
                country,
                seasonalityScore,
                result.getSeasonalityLevel(),
                yearlyAvg
        ));

        // Tìm tháng cao điểm và thấp điểm
        Optional<MonthlyDataDTO> peakMonth = historicalData.stream()
                .max(Comparator.comparing(MonthlyDataDTO::getAvgQuantity));
        Optional<MonthlyDataDTO> lowMonth = historicalData.stream()
                .min(Comparator.comparing(MonthlyDataDTO::getAvgQuantity));

        String peakInfo = peakMonth.map(m -> m.getMonthName()).orElse("N/A");
        String lowInfo = lowMonth.map(m -> m.getMonthName()).orElse("N/A");

        result.setRecommendation(String.format(
                "GỢI Ý: Tháng cao điểm là %s, tháng thấp điểm là %s. " +
                "%s. Dự báo %d tháng tới cho thấy nhu cầu sẽ %s.",
                peakInfo,
                lowInfo,
                result.getSeasonalityLevel().equals("HIGH") ? 
                    "Sản phẩm có tính mùa vụ cao, cần lập kế hoạch nhập hàng cẩn thận" :
                    "Sản phẩm ổn định, dễ quản lý tồn kho",
                forecastMonths,
                forecastData.get(0).getSeasonalityIndex() > 1.2 ? "tăng cao" : 
                    (forecastData.get(0).getSeasonalityIndex() < 0.8 ? "giảm" : "ổn định")
        ));

        return result;
    }
}

