package com.dss.model.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;


@Document(collection = "InvoiceDataset") // Tên collection trong MongoDB
public class RetailData {

    @Id
    private String id; // ID tự động của MongoDB

    @Field("InvoiceNo")
    private String invoiceNo;

    @Field("StockCode")
    private String stockCode;

    @Field("Description")
    private String description;

    @Field("Quantity")
    private int quantity;

    @Field("InvoiceDate")
    private LocalDateTime invoiceDate;

    @Field("UnitPrice")
    private double unitPrice;

    @Field("CustomerID")
    private String customerID;

    @Field("Country")
    private String country;

    // 1. Constructor rỗng
    public RetailData() {}

    // 2. Getters and Setters (cho tất cả các trường)
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getInvoiceNo() {
        return invoiceNo;
    }
    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }
    public String getStockCode() {
        return stockCode;
    }
    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public LocalDateTime getInvoiceDate() {
        return invoiceDate;
    }
    public void setInvoiceDate(LocalDateTime invoiceDate) {
        this.invoiceDate = invoiceDate;
    }
    public double getUnitPrice() {
        return unitPrice;
    }
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
    public String getCustomerID() {
        return customerID;
    }
    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
}