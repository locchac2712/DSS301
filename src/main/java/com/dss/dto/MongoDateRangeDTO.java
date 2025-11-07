package com.dss.dto;

import java.time.LocalDate;

/**
 * MongoDateRangeDTO là class DTO để lưu trữ khoảng thời gian (minDate, maxDate)
 * từ MongoDB.
 * Nó có các phương thức để lấy khoảng thời gian.
 * 
 * @author DSS301
 * @version 1.0
 * @since 2025-11-07
 */
public class MongoDateRangeDTO {
    private final LocalDate minDate;
    private final LocalDate maxDate;

    public MongoDateRangeDTO(LocalDate minDate, LocalDate maxDate) {
        this.minDate = minDate;
        this.maxDate = maxDate;
    }

    public LocalDate getMinDate() {
        return minDate;
    }

    public LocalDate getMaxDate() {
        return maxDate;
    }
}
