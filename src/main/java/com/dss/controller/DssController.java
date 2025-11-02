package com.dss.controller;

import com.dss.dto.DssResultDTO;
import com.dss.model.mongo.RetailData;
import com.dss.service.DiscountDssService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@CrossOrigin
public class DssController {

    private final DiscountDssService dssService;

    public DssController(DiscountDssService dssService) {
        this.dssService = dssService;
    }

    @GetMapping("/api/dss/product-details/{stockCode}")
    @ResponseBody // <-- Rất quan trọng: Báo Spring trả về JSON, không phải HTML
    public ResponseEntity<RetailData> getProductDetails(@PathVariable String stockCode) {

        return dssService.getDetailsByStockCode(stockCode)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 1. Hiển thị trang (GET)
    @GetMapping("/dss/discount")
    public String showDiscountPage(Model model) {
        // Tải dữ liệu cho combobox
        model.addAttribute("dssResult", new DssResultDTO());
        model.addAttribute("countries", dssService.getDistinctCountries());

        // Thêm một đối tượng DssResultDTO rỗng để tránh lỗi Thymeleaf
        model.addAttribute("dssResult", new DssResultDTO());
        return "dss-discount"; // Trả về tệp dss-discount.html
    }

    // 2. Xử lý tính toán (POST)
    @PostMapping("/dss/discount/calculate")
    public String runCalculation(
            @RequestParam("product") String stockCode,
            @RequestParam("country") String country,
            @RequestParam("optimizeOn") String optimizeOn,
            Model model) {

        // Chạy mô phỏng
        DssResultDTO result = dssService.runSimulation(stockCode, country, optimizeOn);

        // Tải lại dữ liệu combobox

        model.addAttribute("countries", dssService.getDistinctCountries());

        // Gửi kết quả (thật) trở lại trang
        model.addAttribute("dssResult", result);

        // Giữ lại lựa chọn của người dùng
        model.addAttribute("selectedStockCode", stockCode);
        model.addAttribute("selectedCountry", country);
        model.addAttribute("selectedOptimizeOn", optimizeOn);

        return "dss-discount";
    }
}