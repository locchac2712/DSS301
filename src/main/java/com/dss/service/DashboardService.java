package com.dss.service;


import com.dss.dto.DashboardKpiDTO;
import com.dss.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final OrderRepository orderRepository;

    public DashboardService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    //lấy dữ liệu KPI
    public DashboardKpiDTO getKpiData(){
        return orderRepository.getKpiData();
    }
}
