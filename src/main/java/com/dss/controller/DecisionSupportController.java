package com.dss.controller;


import com.dss.dto.DecisionSupportDTOs.DiscountResultDTO;
import com.dss.dto.DecisionSupportDTOs.ReorderSuggestionDTO;
import com.dss.service.DecisionSupportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DecisionSupportController {

    private final DecisionSupportService service;

    public DecisionSupportController(DecisionSupportService service) {
        this.service = service;
    }

    /**
     * Optimize discount for a given StockCode.
     * Example: /api/discount/optimize?stockCode=85123A&basePrice=1.5&forecastMonths=6
     */
    @GetMapping("/discount/optimize")
    public ResponseEntity<DiscountResultDTO> optimizeDiscount(
            @RequestParam String stockCode,
            @RequestParam(required = false) Double basePrice,
            @RequestParam(defaultValue = "6") int forecastMonths) {

        DiscountResultDTO dto = service.optimizeDiscount(stockCode, basePrice, forecastMonths);
        return ResponseEntity.ok(dto);
    }

    /**
     * Reorder suggestions across products in collection.
     * Example: /api/reorder/suggestions?leadTimeMonths=1
     */
    @GetMapping("/reorder/suggestions")
    public ResponseEntity<List<ReorderSuggestionDTO>> getReorderSuggestions(
            @RequestParam(defaultValue = "1") int leadTimeMonths) {

        List<ReorderSuggestionDTO> list = service.getReorderSuggestions(leadTimeMonths);
        return ResponseEntity.ok(list);
    }
}
