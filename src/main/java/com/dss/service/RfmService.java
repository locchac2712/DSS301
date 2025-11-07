package com.dss.service;

import com.dss.dto.MongoDateRangeDTO;
import com.dss.dto.PopularProductDTO;
import com.dss.dto.PotentialProductDTO;
import com.dss.dto.RfmResultDTO;
import com.dss.dto.TrendDataDTO;
import com.dss.model.mongo.RetailData;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RfmService là class service để phân tích và dự đoán dữ liệu RFM.
 * Nó có các phương thức để phân tích và dự đoán dữ liệu RFM.
 * 
 * @author DSS301
 * @version 1.0
 * @since 2025-11-07
 */
@Service
public class RfmService {
    private final MongoTemplate mongoTemplate; // Template để lấy dữ liệu từ MongoDB

    public RfmService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public RfmResultDTO analyzeAndPredict(LocalDate startDate, LocalDate endDate,
            String selectedSegment, String timePeriod) {
        // Lấy dữ liệu giao dịch từ MongoDB trong khoảng thời gian
        List<RetailData> retailData = fetchRetailData(startDate, endDate);

        // Tính RFM scores cho từng customer
        Map<String, RfmScores> customerRfmScores = calculateRfmScores(retailData, endDate);

        // Phân loại customers theo segment
        Map<String, List<String>> segmentCustomers = segmentCustomers(customerRfmScores);

        // Lấy customers trong segment được chọn
        List<String> targetCustomers = segmentCustomers.getOrDefault(selectedSegment, Collections.emptyList());

        // Tính RFM trung bình của segment
        Double[] avgScores = calculateAvgRfmScores(customerRfmScores, targetCustomers);

        // Lấy sản phẩm tiềm năng
        List<PotentialProductDTO> potentialProducts = getPotentialProducts(retailData, targetCustomers);

        // Lấy sản phẩm phổ biến
        List<PopularProductDTO> popularProducts = getPopularProducts(retailData, targetCustomers);

        // Tính xu hướng theo thời gian
        List<TrendDataDTO> trendData = getTrendData(retailData, timePeriod);

        // Tạo result
        RfmResultDTO result = new RfmResultDTO();
        result.setSelectedSegment(selectedSegment);
        result.setTimePeriod(timePeriod);
        result.setPotentialProducts(potentialProducts);
        result.setPopularProducts(popularProducts);
        result.setTrendData(trendData);
        result.setAvgRecencyScore(avgScores[0]);
        result.setAvgFrequencyScore(avgScores[1]);
        result.setAvgMonetaryScore(avgScores[2]);

        return result;
    }

    private List<RetailData> fetchRetailData(LocalDate startDate, LocalDate endDate) {
        Query query = new Query();
        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime to = endDate.plusDays(1).atStartOfDay().minusNanos(1);
        Date fromDate = Date.from(from.atZone(ZoneId.systemDefault()).toInstant());
        Date toDate = Date.from(to.atZone(ZoneId.systemDefault()).toInstant());
        Objects.requireNonNull(fromDate, "fromDate must not be null");
        Objects.requireNonNull(toDate, "toDate must not be null");
        query.addCriteria(Criteria.where("InvoiceDate").gte(fromDate).lte(toDate));
        return mongoTemplate.find(query, RetailData.class, "InvoiceDataset");
    }

    public Optional<MongoDateRangeDTO> getMongoDateRange() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group()
                        .min("InvoiceDate").as("minDate")
                        .max("InvoiceDate").as("maxDate"));

        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation,
                "InvoiceDataset",
                Document.class);

        Document doc = results.getUniqueMappedResult();
        if (doc == null) {
            return Optional.empty();
        }

        LocalDate minDate = toLocalDate(doc.get("minDate"));
        LocalDate maxDate = toLocalDate(doc.get("maxDate"));

        if (minDate == null || maxDate == null) {
            return Optional.empty();
        }

        return Optional.of(new MongoDateRangeDTO(minDate, maxDate));
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toLocalDate();
        }
        if (value instanceof Date) {
            return ((Date) value).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (value instanceof String) {
            try {
                return LocalDate.parse((String) value);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private Map<String, RfmScores> calculateRfmScores(List<RetailData> retailData, LocalDate endDate) {
        Map<String, CustomerStats> statsByCustomer = new HashMap<>();

        for (RetailData record : retailData) {
            if (record == null || record.getCustomerID() == null || record.getCustomerID().isBlank()) {
                continue;
            }
            if (record.getQuantity() <= 0) {
                continue;
            }

            CustomerStats stats = statsByCustomer.computeIfAbsent(record.getCustomerID(), key -> new CustomerStats());

            LocalDateTime invoiceDate = record.getInvoiceDate();
            if (invoiceDate != null) {
                if (stats.lastPurchase == null || invoiceDate.isAfter(stats.lastPurchase)) {
                    stats.lastPurchase = invoiceDate;
                }
            }

            if (record.getInvoiceNo() != null) {
                stats.invoiceNos.add(record.getInvoiceNo());
            }

            BigDecimal lineRevenue = BigDecimal.valueOf(record.getUnitPrice())
                    .multiply(BigDecimal.valueOf(record.getQuantity()));
            if (lineRevenue.compareTo(BigDecimal.ZERO) > 0) {
                stats.totalRevenue = stats.totalRevenue.add(lineRevenue);
            }
        }

        if (statsByCustomer.isEmpty()) {
            return Collections.emptyMap();
        }

        DoubleSummaryStatistics recencyStats = new DoubleSummaryStatistics();
        DoubleSummaryStatistics frequencyStats = new DoubleSummaryStatistics();
        DoubleSummaryStatistics monetaryStats = new DoubleSummaryStatistics();

        for (CustomerStats stats : statsByCustomer.values()) {
            LocalDate lastPurchaseDate = stats.lastPurchase != null ? stats.lastPurchase.toLocalDate() : endDate;
            double recencyDays = Math.max(0,
                    java.time.temporal.ChronoUnit.DAYS.between(lastPurchaseDate, endDate));
            stats.recencyDays = recencyDays;
            recencyStats.accept(recencyDays);

            double frequencyCount = stats.invoiceNos.size();
            stats.frequencyCount = frequencyCount;
            frequencyStats.accept(frequencyCount);

            double monetaryValue = stats.totalRevenue.doubleValue();
            stats.monetaryValue = monetaryValue;
            monetaryStats.accept(monetaryValue);
        }

        double minRecency = recencyStats.getCount() > 0 ? recencyStats.getMin() : 0.0;
        double maxRecency = recencyStats.getCount() > 0 ? recencyStats.getMax() : minRecency;
        double minFrequency = frequencyStats.getCount() > 0 ? frequencyStats.getMin() : 0.0;
        double maxFrequency = frequencyStats.getCount() > 0 ? frequencyStats.getMax() : minFrequency;
        double minMonetary = monetaryStats.getCount() > 0 ? monetaryStats.getMin() : 0.0;
        double maxMonetary = monetaryStats.getCount() > 0 ? monetaryStats.getMax() : minMonetary;

        Map<String, RfmScores> scores = new HashMap<>();
        for (Map.Entry<String, CustomerStats> entry : statsByCustomer.entrySet()) {
            CustomerStats stats = entry.getValue();
            double recencyScore = scaleToFive(stats.recencyDays, minRecency, maxRecency, true);
            double frequencyScore = scaleToFive(stats.frequencyCount, minFrequency, maxFrequency, false);
            double monetaryScore = scaleToFive(stats.monetaryValue, minMonetary, maxMonetary, false);
            scores.put(entry.getKey(), new RfmScores(recencyScore, frequencyScore, monetaryScore));
        }

        return scores;
    }

    private Map<String, List<String>> segmentCustomers(Map<String, RfmScores> customerRfmScores) {
        Map<String, List<String>> segments = new HashMap<>();
        segments.put("ALL", new ArrayList<>());
        segments.put("Champions", new ArrayList<>());
        segments.put("Loyal Customers", new ArrayList<>());
        segments.put("New Customers", new ArrayList<>());
        segments.put("At Risk", new ArrayList<>());

        for (Map.Entry<String, RfmScores> entry : customerRfmScores.entrySet()) {
            String customerId = entry.getKey();
            RfmScores scores = entry.getValue();

            segments.get("ALL").add(customerId);

            // Phân loại theo RFM scores
            if (scores.recency >= 4 && scores.frequency >= 4 && scores.monetary >= 4) {
                segments.get("Champions").add(customerId);
            } else if (scores.frequency >= 3 && scores.monetary >= 3) {
                segments.get("Loyal Customers").add(customerId);
            } else if (scores.recency <= 1) {
                segments.get("New Customers").add(customerId);
            } else if (scores.recency >= 3 && scores.frequency <= 2) {
                segments.get("At Risk").add(customerId);
            }
        }

        return segments;
    }

    private Double[] calculateAvgRfmScores(Map<String, RfmScores> customerRfmScores, List<String> targetCustomers) {
        if (targetCustomers.isEmpty()) {
            return new Double[] { 0.0, 0.0, 0.0 };
        }

        double sumRecency = 0, sumFrequency = 0, sumMonetary = 0;
        int count = 0;

        for (String customerId : targetCustomers) {
            RfmScores scores = customerRfmScores.get(customerId);
            if (scores != null) {
                sumRecency += scores.recency;
                sumFrequency += scores.frequency;
                sumMonetary += scores.monetary;
                count++;
            }
        }

        if (count == 0) {
            return new Double[] { 0.0, 0.0, 0.0 };
        }

        return new Double[] {
                sumRecency / count,
                sumFrequency / count,
                sumMonetary / count
        };
    }

    private List<PotentialProductDTO> getPotentialProducts(List<RetailData> retailData, List<String> targetCustomers) {
        if (targetCustomers == null || targetCustomers.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> customerFilter = new HashSet<>(targetCustomers);
        Map<String, ProductStats> productStats = new HashMap<>();

        for (RetailData record : retailData) {
            if (record == null || record.getCustomerID() == null || record.getCustomerID().isBlank()) {
                continue;
            }
            if (!customerFilter.contains(record.getCustomerID())) {
                continue;
            }
            if (record.getQuantity() <= 0) {
                continue;
            }
            if (record.getStockCode() == null || record.getStockCode().isBlank()) {
                continue;
            }

            ProductStats stats = productStats.computeIfAbsent(record.getStockCode(), key -> new ProductStats());

            BigDecimal lineRevenue = BigDecimal.valueOf(record.getUnitPrice())
                    .multiply(BigDecimal.valueOf(record.getQuantity()));
            if (lineRevenue.compareTo(BigDecimal.ZERO) > 0) {
                stats.totalRevenue = stats.totalRevenue.add(lineRevenue);
            }

            if (record.getInvoiceNo() != null) {
                stats.invoiceNos.add(record.getInvoiceNo());
            }

            if (record.getCustomerID() != null) {
                stats.customerIds.add(record.getCustomerID());
            }

            if (stats.description == null || stats.description.isBlank()) {
                stats.description = record.getDescription();
            }
        }

        return productStats.entrySet().stream()
                .map(entry -> {
                    ProductStats stats = entry.getValue();
                    double potentialScore = stats.totalRevenue.doubleValue() * stats.invoiceNos.size() / 100.0;
                    potentialScore = Math.min(100.0, potentialScore);

                    PotentialProductDTO dto = new PotentialProductDTO();
                    dto.setStockCode(entry.getKey());
                    dto.setDescription(stats.description != null ? stats.description : entry.getKey());
                    dto.setPotentialScore(Math.round(potentialScore * 10.0) / 10.0);
                    return dto;
                })
                .sorted((a, b) -> Double.compare(b.getPotentialScore(), a.getPotentialScore()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<PopularProductDTO> getPopularProducts(List<RetailData> retailData, List<String> targetCustomers) {
        if (targetCustomers == null || targetCustomers.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> customerFilter = new HashSet<>(targetCustomers);
        Map<String, ProductStats> productStats = new HashMap<>();
        int totalCustomers = customerFilter.size();

        for (RetailData record : retailData) {
            if (record == null || record.getCustomerID() == null || record.getCustomerID().isBlank()) {
                continue;
            }
            if (!customerFilter.contains(record.getCustomerID())) {
                continue;
            }
            if (record.getQuantity() <= 0) {
                continue;
            }
            if (record.getStockCode() == null || record.getStockCode().isBlank()) {
                continue;
            }

            ProductStats stats = productStats.computeIfAbsent(record.getStockCode(), key -> new ProductStats());

            BigDecimal lineRevenue = BigDecimal.valueOf(record.getUnitPrice())
                    .multiply(BigDecimal.valueOf(record.getQuantity()));
            if (lineRevenue.compareTo(BigDecimal.ZERO) > 0) {
                stats.totalRevenue = stats.totalRevenue.add(lineRevenue);
            }

            if (record.getInvoiceNo() != null) {
                stats.invoiceNos.add(record.getInvoiceNo());
            }
            stats.customerIds.add(record.getCustomerID());

            if (stats.description == null || stats.description.isBlank()) {
                stats.description = record.getDescription();
            }
        }

        return productStats.entrySet().stream()
                .map(entry -> {
                    ProductStats stats = entry.getValue();
                    double purchaseRate = totalCustomers > 0
                            ? (stats.customerIds.size() * 100.0 / totalCustomers)
                            : 0.0;

                    PopularProductDTO dto = new PopularProductDTO();
                    dto.setStockCode(entry.getKey());
                    dto.setDescription(stats.description != null ? stats.description : entry.getKey());
                    dto.setPurchaseCount((long) stats.invoiceNos.size());
                    dto.setTotalRevenue(stats.totalRevenue);
                    dto.setPurchaseRate(Math.round(purchaseRate * 10.0) / 10.0);
                    return dto;
                })
                .sorted((a, b) -> Long.compare(b.getPurchaseCount(), a.getPurchaseCount()))
                .limit(20)
                .collect(Collectors.toList());
    }

    private List<TrendDataDTO> getTrendData(List<RetailData> retailData, String timePeriod) {
        Map<String, PeriodStats> periodStats = new HashMap<>();
        DateTimeFormatter formatter;

        switch (timePeriod) {
            case "QUARTER":
                formatter = DateTimeFormatter.ofPattern("yyyy-'Q'Q");
                break;
            case "YEAR":
                formatter = DateTimeFormatter.ofPattern("yyyy");
                break;
            default: // MONTH
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        }

        for (RetailData record : retailData) {
            if (record == null || record.getInvoiceDate() == null) {
                continue;
            }
            if (record.getQuantity() <= 0) {
                continue;
            }

            String period = record.getInvoiceDate().toLocalDate().format(formatter);
            PeriodStats stats = periodStats.computeIfAbsent(period, key -> new PeriodStats());

            BigDecimal lineRevenue = BigDecimal.valueOf(record.getUnitPrice())
                    .multiply(BigDecimal.valueOf(record.getQuantity()));
            if (lineRevenue.compareTo(BigDecimal.ZERO) > 0) {
                stats.totalRevenue = stats.totalRevenue.add(lineRevenue);
            }

            if (record.getInvoiceNo() != null) {
                stats.invoiceNos.add(record.getInvoiceNo());
            }

            if (record.getCustomerID() != null && !record.getCustomerID().isBlank()) {
                stats.customerIds.add(record.getCustomerID());
            }
        }

        List<TrendDataDTO> trendData = new ArrayList<>();
        List<String> sortedPeriods = new ArrayList<>(periodStats.keySet());
        Collections.sort(sortedPeriods);

        int predictionStart = Math.max(sortedPeriods.size() - 2, 0);

        for (int i = 0; i < sortedPeriods.size(); i++) {
            String period = sortedPeriods.get(i);
            PeriodStats stats = periodStats.get(period);
            boolean isPredicted = i >= predictionStart && sortedPeriods.size() >= 3;

            TrendDataDTO dto = new TrendDataDTO();
            dto.setPeriod(period);
            dto.setTotalRevenue(stats.totalRevenue);
            dto.setTotalOrders((long) stats.invoiceNos.size());
            dto.setTotalCustomers((long) stats.customerIds.size());
            if (isPredicted) {
                BigDecimal predicted = stats.totalRevenue.multiply(BigDecimal.valueOf(1.1));
                dto.setPredictedRevenue(predicted);
            } else {
                dto.setPredictedRevenue(BigDecimal.ZERO);
            }
            trendData.add(dto);
        }

        return trendData;
    }

    private double scaleToFive(double value, double min, double max, boolean inverse) {
        if (Double.isNaN(value)) {
            return 0.0;
        }
        if (max <= min) {
            return 5.0;
        }

        double normalized = (value - min) / (max - min);
        normalized = Math.max(0.0, Math.min(1.0, normalized));
        if (inverse) {
            normalized = 1.0 - normalized;
        }

        double score = 1.0 + normalized * 4.0;
        return Math.round(score * 10.0) / 10.0;
    }

    // Helper classes
    private static class RfmScores {
        double recency;
        double frequency;
        double monetary;

        RfmScores(double recency, double frequency, double monetary) {
            this.recency = recency;
            this.frequency = frequency;
            this.monetary = monetary;
        }
    }

    private static class ProductStats {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        Set<String> invoiceNos = new HashSet<>();
        Set<String> customerIds = new HashSet<>();
        String description;
    }

    private static class PeriodStats {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        Set<String> invoiceNos = new HashSet<>();
        Set<String> customerIds = new HashSet<>();
    }

    private static class CustomerStats {
        LocalDateTime lastPurchase;
        Set<String> invoiceNos = new HashSet<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        double recencyDays;
        double frequencyCount;
        double monetaryValue;
    }
}
