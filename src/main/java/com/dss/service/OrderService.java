package com.dss.service;

import com.dss.model.Order;
import com.dss.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository repo;

    public OrderService(OrderRepository repo) {
        this.repo = repo;
    }

    public List<Order> getAll() {
        return repo.findAll();
    }

    /**
     * Lấy một TRANG (Page) đơn hàng với phân trang
     */
    public Page<Order> getOrders(Pageable pageable) {
        return repo.findAll(pageable);
    }

    /**
     * Lấy đơn hàng theo ID
     */
    public Optional<Order> getById(String id) {
        return repo.findById(id);
    }
}
