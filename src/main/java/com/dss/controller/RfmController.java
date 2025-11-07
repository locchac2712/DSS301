package com.dss.controller;

import com.dss.dto.MongoDateRangeDTO;
import com.dss.dto.RfmResultDTO;
import com.dss.service.RfmService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
public class RfmController {
    private final RfmService rfmService;

    public RfmController(RfmService rfmService) {
        this.rfmService = rfmService;
    }

    @GetMapping("/rfm/prediction")
    public String showRfmPage(HttpSession session, Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        // Danh sách segments (loại bỏ 'ALL')
        List<String> segments = Arrays.asList("Champions", "Loyal Customers", "New Customers", "At Risk");
        model.addAttribute("segments", segments);

        rfmService.getMongoDateRange().ifPresent(range -> {
            model.addAttribute("dateRange", range);
            if (!model.containsAttribute("selectedStartDate")) {
                model.addAttribute("selectedStartDate", range.getMinDate().toString());
            }
            if (!model.containsAttribute("selectedEndDate")) {
                model.addAttribute("selectedEndDate", range.getMaxDate().toString());
            }
        });

        if (!model.containsAttribute("selectedSegment")) {
            model.addAttribute("selectedSegment", "Champions");
        }
        if (!model.containsAttribute("selectedTimePeriod")) {
            model.addAttribute("selectedTimePeriod", "MONTH");
        }

        return "rfm-prediction";
    }

    @PostMapping("/rfm/prediction")
    public String analyzeRfm(@RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String selectedSegment,
            @RequestParam String timePeriod,
            @RequestParam(required = false, defaultValue = "Prophet") String predictionModel,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            if (session.getAttribute("user") == null) {
                return "redirect:/login";
            }

            // Parse dates
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);

            // Validate dates
            if (start.isAfter(end)) {
                redirectAttributes.addFlashAttribute("selectedStartDate", startDate);
                redirectAttributes.addFlashAttribute("selectedEndDate", endDate);
                redirectAttributes.addFlashAttribute("selectedSegment", selectedSegment);
                redirectAttributes.addFlashAttribute("selectedTimePeriod", timePeriod);
                redirectAttributes.addFlashAttribute("error", "Ngày bắt đầu phải trước ngày kết thúc!");
                return "redirect:/rfm/prediction";
            }

            Optional<MongoDateRangeDTO> dateRangeOpt = rfmService.getMongoDateRange();
            dateRangeOpt.ifPresent(range -> {
                model.addAttribute("dateRange", range);
            });

            if (dateRangeOpt.isPresent()) {
                MongoDateRangeDTO range = dateRangeOpt.get();
                if (start.isBefore(range.getMinDate()) || end.isAfter(range.getMaxDate())) {
                    redirectAttributes.addFlashAttribute("selectedStartDate", startDate);
                    redirectAttributes.addFlashAttribute("selectedEndDate", endDate);
                    redirectAttributes.addFlashAttribute("selectedSegment", selectedSegment);
                    redirectAttributes.addFlashAttribute("selectedTimePeriod", timePeriod);
                    redirectAttributes.addFlashAttribute("error",
                            String.format("Ngày phải nằm trong khoảng từ %s đến %s!",
                                    range.getMinDate(), range.getMaxDate()));
                    return "redirect:/rfm/prediction";
                }
            }

            // Phân tích RFM
            RfmResultDTO result = rfmService.analyzeAndPredict(start, end, selectedSegment, timePeriod);

            // Danh sách segments (loại bỏ 'ALL')
            List<String> segments = Arrays.asList("Champions", "Loyal Customers", "New Customers", "At Risk");
            model.addAttribute("segments", segments);
            model.addAttribute("selectedSegment", selectedSegment);
            model.addAttribute("selectedStartDate", startDate);
            model.addAttribute("selectedEndDate", endDate);
            model.addAttribute("selectedTimePeriod", timePeriod);
            model.addAttribute("rfmResult", result);

            return "rfm-prediction";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/rfm/prediction";
        }
    }
}
