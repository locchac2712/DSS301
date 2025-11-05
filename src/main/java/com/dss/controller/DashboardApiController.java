package com.dss.controller;

import com.dss.dto.CategoryRevenueDTO;
import com.dss.dto.DashboardKpiDTO;
import com.dss.dto.TimeSeriesDataDTO;
import com.dss.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class    DashboardApiController {
    private final DashboardService dashboardService;

    public DashboardApiController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

//    API endponts để lấy dữ liệu JSON cho 4 thẻ KPI
    @GetMapping("/kpis")
    public ResponseEntity<DashboardKpiDTO> getKpi(){
        DashboardKpiDTO kpiData = dashboardService.getKpiData();
        return ResponseEntity.ok(kpiData);
    }

    //API endpoint để lấy dữ liệu cho biểu đồ tròn
    @GetMapping("/revenue-by-category")
    public ResponseEntity<List<CategoryRevenueDTO>> getRevenueByCategory() {
        List<CategoryRevenueDTO> data = dashboardService.getRevenueByCategory();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/sales-over-time")
    public ResponseEntity<List<TimeSeriesDataDTO>> getSalesOverTime() {
        List<TimeSeriesDataDTO> data = dashboardService.getSalesRevenueOverTime();
        return ResponseEntity.ok(data);
    }
}


