package com.dss.service;

import com.dss.dto.DecisionSupportDTOs.DiscountChartPoint;
import com.dss.dto.DecisionSupportDTOs.DiscountResultDTO;
import com.dss.dto.DecisionSupportDTOs.ReorderSuggestionDTO;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class DecisionSupportService {

    private static final Logger log = LoggerFactory.getLogger(DecisionSupportService.class);
    private final MongoTemplate mongoTemplate;
    private static final String COLLECTION_NAME = "order_items";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DecisionSupportService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        log.info("=== DecisionSupportService initialized ===");
        log.info("Collection name: {}", COLLECTION_NAME);

        try {
            boolean exists = mongoTemplate.collectionExists(COLLECTION_NAME);
            log.info("Collection exists: {}", exists);

            if (exists) {
                long count = mongoTemplate.getCollection(COLLECTION_NAME).countDocuments();
                log.info("Total documents in collection: {}", count);

                Document sample = mongoTemplate.getCollection(COLLECTION_NAME).find().first();
                if (sample != null) {
                    log.info("Sample document fields: {}", sample.keySet());
                }
            }
        } catch (Exception e) {
            log.error("Error connecting to MongoDB: {}", e.getMessage(), e);
        }
    }

    public DiscountResultDTO optimizeDiscount(String stockCode, Double basePriceParam, int forecastMonths) {
        log.info("=== optimizeDiscount called ===");
        log.info("StockCode: {}, basePrice: {}, forecastMonths: {}", stockCode, basePriceParam, forecastMonths);

        try {
            MatchOperation match = Aggregation.match(Criteria.where("StockCode").is(stockCode));
            GroupOperation group = Aggregation.group("StockCode")
                    .first("Description").as("description")
                    .sum("Quantity").as("totalQuantity")
                    .avg("UnitPrice").as("avgUnitPrice")
                    .min("InvoiceDate").as("minDate")
                    .max("InvoiceDate").as("maxDate");

            Aggregation agg = Aggregation.newAggregation(match, group);
            AggregationResults<Document> results = mongoTemplate.aggregate(agg, COLLECTION_NAME, Document.class);
            Document doc = results.getUniqueMappedResult();

            log.info("Query result: {}", doc != null ? "Found" : "Not found");

            if (doc == null) {
                log.warn("No data found for StockCode: {}", stockCode);
                return createDefaultResult(stockCode, basePriceParam, forecastMonths);
            }

            log.info("Document: {}", doc.toJson());

            String description = doc.getString("description");
            if (description == null) description = stockCode;

            // Get total quantity
            Object totalQtyObj = doc.get("totalQuantity");
            double totalQty = 0;
            if (totalQtyObj instanceof Number) {
                totalQty = ((Number) totalQtyObj).doubleValue();
            }
            log.info("Total quantity: {}", totalQty);

            // Get average unit price
            double avgUnitPrice = basePriceParam != null ? basePriceParam : 0.0;
            if (basePriceParam == null) {
                Object avgPriceObj = doc.get("avgUnitPrice");
                if (avgPriceObj instanceof Number) {
                    avgUnitPrice = ((Number) avgPriceObj).doubleValue();
                }
            }
            log.info("Average unit price: {}", avgUnitPrice);

            // Calculate average monthly sales
            double avgMonthlySales = 1.0;
            Object minDateObj = doc.get("minDate");
            Object maxDateObj = doc.get("maxDate");

            if (minDateObj != null && maxDateObj != null && totalQty > 0) {
                LocalDate minDate = parseDate(minDateObj);
                LocalDate maxDate = parseDate(maxDateObj);

                if (minDate != null && maxDate != null) {
                    minDate = minDate.withDayOfMonth(1);
                    maxDate = maxDate.withDayOfMonth(1);
                    long months = ChronoUnit.MONTHS.between(minDate, maxDate) + 1;
                    if (months <= 0) months = 1;
                    avgMonthlySales = totalQty / (double) months;
                    log.info("Date range: {} to {}, months: {}, avgMonthlySales: {}", minDate, maxDate, months, avgMonthlySales);
                } else {
                    avgMonthlySales = Math.max(1.0, totalQty / 3.0);
                }
            } else {
                avgMonthlySales = Math.max(1.0, totalQty / 3.0);
            }

            double basePrice = avgUnitPrice;
            if (basePriceParam != null && basePriceParam > 0.0) {
                basePrice = basePriceParam;
            }
            if (basePrice <= 0) basePrice = 1.0; // fallback

            // Simulation
            int[] levels = new int[]{0,5,10,15,20,25,30,35,40};
            List<DiscountChartPoint> chart = new ArrayList<>();
            double costRatio = 0.6;
            double costPerUnit = basePrice * costRatio;

            double bestProfit = Double.NEGATIVE_INFINITY;
            int bestLevel = 0;
            double bestRevenue = 0;
            double bestQty = 0;

            for (int l : levels) {
                double priceAfter = basePrice * (1.0 - l / 100.0);
                double elasticityFactor = 1.0 + (l / 100.0) * 1.5;
                double qty = avgMonthlySales * elasticityFactor * Math.max(1, forecastMonths);
                double revenue = priceAfter * qty;
                double profit = (priceAfter - costPerUnit) * qty;

                chart.add(new DiscountChartPoint(l, qty,
                        BigDecimal.valueOf(revenue).setScale(2, BigDecimal.ROUND_HALF_UP),
                        BigDecimal.valueOf(profit).setScale(2, BigDecimal.ROUND_HALF_UP)));

                if (profit > bestProfit) {
                    bestProfit = profit;
                    bestLevel = l;
                    bestRevenue = revenue;
                    bestQty = qty;
                }
            }

            log.info("Best discount: {}%, profit: {}", bestLevel, bestProfit);

            DiscountResultDTO result = new DiscountResultDTO();
            result.setStockCode(stockCode);
            result.setProductName(description);
            result.setAvgMonthlySales(avgMonthlySales);
            result.setBasePrice(BigDecimal.valueOf(basePrice).setScale(2, BigDecimal.ROUND_HALF_UP));
            result.setForecastMonths(forecastMonths);
            result.setCostPerUnit(BigDecimal.valueOf(costPerUnit).setScale(2, BigDecimal.ROUND_HALF_UP));
            result.setChart(chart);
            result.setOptimalDiscountPercent(bestLevel);
            result.setOptimalRevenue(BigDecimal.valueOf(bestRevenue).setScale(2, BigDecimal.ROUND_HALF_UP));
            result.setOptimalProfit(BigDecimal.valueOf(bestProfit).setScale(2, BigDecimal.ROUND_HALF_UP));
            result.setOptimalQuantity(bestQty);

            return result;

        } catch (Exception e) {
            log.error("Error in optimizeDiscount: {}", e.getMessage(), e);
            return createDefaultResult(stockCode, basePriceParam, forecastMonths);
        }
    }

    public List<ReorderSuggestionDTO> getReorderSuggestions(int leadTimeMonths) {
        log.info("=== getReorderSuggestions called ===");
        log.info("Lead time months: {}", leadTimeMonths);

        try {
            GroupOperation group = Aggregation.group("StockCode")
                    .first("Description").as("description")
                    .sum("Quantity").as("totalQuantity")
                    .avg("UnitPrice").as("avgUnitPrice")
                    .min("InvoiceDate").as("minDate")
                    .max("InvoiceDate").as("maxDate");

            Aggregation agg = Aggregation.newAggregation(group);
            AggregationResults<Document> results = mongoTemplate.aggregate(agg, COLLECTION_NAME, Document.class);

            List<Document> docs = results.getMappedResults();
            log.info("Total products found: {}", docs.size());

            List<ReorderSuggestionDTO> suggestions = new ArrayList<>();
            int count = 0;

            for (Document d : docs) {
                try {
                    String stockCode = d.getString("_id");
                    if (stockCode == null) stockCode = "UNKNOWN";

                    String description = d.getString("description");
                    if (description == null) description = stockCode;

                    Object totalQtyObj = d.get("totalQuantity");
                    double totalQty = 0;
                    if (totalQtyObj instanceof Number) {
                        totalQty = ((Number) totalQtyObj).doubleValue();
                    }

                    if (totalQty <= 0) continue; // Skip invalid data

                    Object minDateObj = d.get("minDate");
                    Object maxDateObj = d.get("maxDate");

                    double avgMonthly = 1.0;
                    if (minDateObj != null && maxDateObj != null) {
                        LocalDate minDate = parseDate(minDateObj);
                        LocalDate maxDate = parseDate(maxDateObj);

                        if (minDate != null && maxDate != null) {
                            minDate = minDate.withDayOfMonth(1);
                            maxDate = maxDate.withDayOfMonth(1);
                            long months = ChronoUnit.MONTHS.between(minDate, maxDate) + 1;
                            if (months <= 0) months = 1;
                            avgMonthly = totalQty / (double) months;
                        } else {
                            avgMonthly = Math.max(1.0, totalQty / 3.0);
                        }
                    } else {
                        avgMonthly = Math.max(1.0, totalQty / 3.0);
                    }

                    int currentStockEstimate = (int) Math.round(avgMonthly * 0.5);
                    double forecastDemand = avgMonthly * leadTimeMonths;
                    int reorderQty = 0;
                    if (currentStockEstimate < Math.ceil(forecastDemand)) {
                        reorderQty = (int) Math.ceil(forecastDemand - currentStockEstimate);
                    }

                    if (reorderQty > 0) {
                        ReorderSuggestionDTO dto = new ReorderSuggestionDTO();
                        dto.setStockCode(stockCode);
                        dto.setProductName(description);
                        dto.setAvgMonthlySales(avgMonthly);
                        dto.setCurrentStockEstimate(currentStockEstimate);
                        dto.setForecastDemand((int) Math.round(forecastDemand));
                        dto.setReorderQuantity(reorderQty);
                        suggestions.add(dto);
                        count++;
                    }
                } catch (Exception e) {
                    log.warn("Error processing document: {}", e.getMessage());
                }
            }

            log.info("Total reorder suggestions: {}", count);
            return suggestions;

        } catch (Exception e) {
            log.error("Error in getReorderSuggestions: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private LocalDate parseDate(Object dateObj) {
        try {
            if (dateObj instanceof java.util.Date) {
                return ((java.util.Date) dateObj).toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            } else if (dateObj instanceof String) {
                String dateStr = (String) dateObj;
                // Try parsing "2010-12-01 08:26:00" format
                return LocalDateTime.parse(dateStr, DATE_FORMATTER).toLocalDate();
            }
        } catch (Exception e) {
            log.warn("Cannot parse date: {}, error: {}", dateObj, e.getMessage());
        }
        return null;
    }

    private DiscountResultDTO createDefaultResult(String stockCode, Double basePriceParam, int forecastMonths) {
        double basePrice = basePriceParam != null && basePriceParam > 0 ? basePriceParam : 10.0;
        double avgMonthlySales = 10.0;

        int[] levels = new int[]{0,5,10,15,20,25,30,35,40};
        List<DiscountChartPoint> chart = new ArrayList<>();
        double costRatio = 0.6;
        double costPerUnit = basePrice * costRatio;

        double bestProfit = Double.NEGATIVE_INFINITY;
        int bestLevel = 0;
        double bestRevenue = 0;
        double bestQty = 0;

        for (int l : levels) {
            double priceAfter = basePrice * (1.0 - l / 100.0);
            double elasticityFactor = 1.0 + (l / 100.0) * 1.5;
            double qty = avgMonthlySales * elasticityFactor * Math.max(1, forecastMonths);
            double revenue = priceAfter * qty;
            double profit = (priceAfter - costPerUnit) * qty;

            chart.add(new DiscountChartPoint(l, qty,
                    BigDecimal.valueOf(revenue).setScale(2, BigDecimal.ROUND_HALF_UP),
                    BigDecimal.valueOf(profit).setScale(2, BigDecimal.ROUND_HALF_UP)));

            if (profit > bestProfit) {
                bestProfit = profit;
                bestLevel = l;
                bestRevenue = revenue;
                bestQty = qty;
            }
        }

        DiscountResultDTO result = new DiscountResultDTO();
        result.setStockCode(stockCode);
        result.setProductName("Sample Product (No data found)");
        result.setAvgMonthlySales(avgMonthlySales);
        result.setBasePrice(BigDecimal.valueOf(basePrice).setScale(2, BigDecimal.ROUND_HALF_UP));
        result.setForecastMonths(forecastMonths);
        result.setCostPerUnit(BigDecimal.valueOf(costPerUnit).setScale(2, BigDecimal.ROUND_HALF_UP));
        result.setChart(chart);
        result.setOptimalDiscountPercent(bestLevel);
        result.setOptimalRevenue(BigDecimal.valueOf(bestRevenue).setScale(2, BigDecimal.ROUND_HALF_UP));
        result.setOptimalProfit(BigDecimal.valueOf(bestProfit).setScale(2, BigDecimal.ROUND_HALF_UP));
        result.setOptimalQuantity(bestQty);

        return result;
    }
}