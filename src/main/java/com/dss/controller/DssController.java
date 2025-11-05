package com.dss.controller; // (1) Đảm bảo tên gói (package) là com.dss.controller

// (2) Import TẤT CẢ các thư viện cần thiết
import com.dss.model.mongo.RetailData;
import com.dss.service.DiscountDssService;
import com.dss.dto.DssResultDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@CrossOrigin // Cho phép AJAX
public class DssController {

    private final DiscountDssService dssService;

    public DssController(DiscountDssService dssService) {
        this.dssService = dssService;
    }

    /**
     * API CHO AJAX (Lấy tên sản phẩm)
     */
    @GetMapping("/api/dss/product-details/{stockCode}")
    @ResponseBody
    public ResponseEntity<RetailData> getProductDetails(@PathVariable String stockCode) {
        return dssService.getDetailsByStockCode(stockCode)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * TRANG DSS (TỐI ƯU GIẢM GIÁ) - HIỂN THỊ
     */
    @GetMapping("/dss/discount")
    public String showDiscountPage(Model model) {
        // (Chúng ta không tải StockCode nữa)
        model.addAttribute("countries", dssService.getDistinctCountries());
        model.addAttribute("dssResult", new DssResultDTO());
        return "dss-discount";
    }

    /**
     * TRANG DSS (TỐI ƯU GIẢM GIÁ) - XỬ LÝ
     */
    @PostMapping("/dss/discount/calculate")
    public String runCalculation(
            @RequestParam("product") String stockCode,
            @RequestParam("country") String country,
            @RequestParam("optimizeOn") String optimizeOn,
            Model model) {

        DssResultDTO result = dssService.runSimulation(stockCode, country, optimizeOn);

        model.addAttribute("countries", dssService.getDistinctCountries());
        model.addAttribute("dssResult", result);
        model.addAttribute("selectedStockCode", stockCode);
        model.addAttribute("selectedCountry", country);
        model.addAttribute("selectedOptimizeOn", optimizeOn);

        return "dss-discount";
    }

    /**
     * TRANG MONGO DATA VIEWER
     */
    @GetMapping("/mongo/list")
    public String showMongoDataList(
            Model model,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "1") int page,

            // (1) SỬA DÒNG NÀY:
            // Đổi defaultValue từ "20" thành "5"
            @RequestParam(name = "size", defaultValue = "5") int size) {

        // (Sắp xếp theo Ngày hóa đơn (InvoiceDate) giảm dần)
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("invoiceDate").descending());

        Page<RetailData> dataPage = dssService.getMongoDataPaginated(keyword, pageable);

        model.addAttribute("dataPage", dataPage);
        model.addAttribute("keyword", keyword);

        // (Logic tạo số trang giữ nguyên)
        int totalPages = dataPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            // Giới hạn số lượng trang hiển thị (ví dụ: chỉ 10 trang)
            if (totalPages > 10) {
                int start = Math.max(1, page - 4);
                int end = Math.min(totalPages, page + 4);
                if (end - start < 9) {
                    start = Math.max(1, end - 9);
                }
                pageNumbers = IntStream.rangeClosed(start, end)
                        .boxed()
                        .collect(Collectors.toList());
            }
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "mongo-data-list";
    }
}