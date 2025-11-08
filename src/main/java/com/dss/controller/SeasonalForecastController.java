package com.dss.controller;

import com.dss.dto.SeasonalForecastResultDTO;
import com.dss.service.SeasonalForecastService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@CrossOrigin
public class SeasonalForecastController {

    private final SeasonalForecastService forecastService;

    public SeasonalForecastController(SeasonalForecastService forecastService) {
        this.forecastService = forecastService;
    }

    // 1. Hiển thị trang (GET)
    @GetMapping("/dss/seasonal-forecast")
    public String showSeasonalForecastPage(Model model) {
        // Tải dữ liệu cho combobox
        model.addAttribute("stockCodes", forecastService.getDistinctStockCodes());
        model.addAttribute("countries", forecastService.getDistinctCountries());

        // Thêm đối tượng rỗng để tránh lỗi Thymeleaf
        model.addAttribute("result", new SeasonalForecastResultDTO());
        
        return "seasonal-forecast";
    }

    // 2. Xử lý phân tích (POST)
    @PostMapping("/dss/seasonal-forecast/analyze")
    public String runAnalysis(
            @RequestParam("stockCode") String stockCode,
            @RequestParam("country") String country,
            @RequestParam(value = "forecastMonths", defaultValue = "6") int forecastMonths,
            Model model) {

        // Chạy phân tích
        SeasonalForecastResultDTO result = forecastService.analyzeSeasonalDemand(
                stockCode, 
                country, 
                forecastMonths
        );

        // Tải lại dữ liệu combobox
        model.addAttribute("stockCodes", forecastService.getDistinctStockCodes());
        model.addAttribute("countries", forecastService.getDistinctCountries());

        // Gửi kết quả trở lại trang
        model.addAttribute("result", result);

        // Giữ lại lựa chọn của người dùng
        model.addAttribute("selectedStockCode", stockCode);
        model.addAttribute("selectedCountry", country);
        model.addAttribute("selectedForecastMonths", forecastMonths);

        return "seasonal-forecast";
    }
}

