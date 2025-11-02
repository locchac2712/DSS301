package com.dss.controller;

import com.dss.dto.DashboardKpiDTO;
import com.dss.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {
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
}


