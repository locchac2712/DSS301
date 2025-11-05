package com.dss.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TimeSeriesDataDTO {

    private LocalDate period; // Ngày (trục X)
    private double total;     // Tổng doanh thu (trục Y)

    // Constructor rỗng
    public TimeSeriesDataDTO() {}

    /**
     * Constructor này sẽ được gọi bởi truy vấn JPQL (PostgreSQL)
     * (LocalDate, BigDecimal)
     */
    public TimeSeriesDataDTO(LocalDate period, BigDecimal total) {
        this.period = period;
        this.total = (total != null) ? total.doubleValue() : 0.0;
    }

    // --- Getters and Setters ---
    public LocalDate getPeriod() {
        return period;
    }
    public void setPeriod(LocalDate period) {
        this.period = period;
    }
    public double getTotal() {
        return total;
    }
    public void setTotal(double total) {
        this.total = total;
    }
}