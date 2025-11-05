package com.dss.service;


import com.dss.dto.CategoryRevenueDTO;
import com.dss.dto.DashboardKpiDTO;
import com.dss.dto.TimeSeriesDataDTO;
import com.dss.repository.OrderItemRepository;
import com.dss.repository.OrderRepository;
import com.dss.repository.mongo.RetailDataRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {
    private final OrderRepository orderRepository;
    private final RetailDataRepository retailDataRepository;
    private final OrderItemRepository orderItemRepository;


    public DashboardService(OrderRepository orderRepository, RetailDataRepository retailDataRepository, OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
        this.retailDataRepository = retailDataRepository;
        this.orderRepository = orderRepository;
    }

    //lấy dữ liệu KPI
    public DashboardKpiDTO getKpiData(){
        return orderRepository.getKpiData();
    }

    public List<CategoryRevenueDTO> getRevenueByCategory(){

        return orderItemRepository.getRevenueByCategoryFromPostgres();
    }
    /**
     * Lấy dữ liệu cho biểu đồ đường Doanh thu theo Thời gian
     */
    public List<TimeSeriesDataDTO> getSalesRevenueOverTime() {
        return orderRepository.getSalesRevenueOverTime();
    }
}
