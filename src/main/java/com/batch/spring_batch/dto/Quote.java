package com.batch.spring_batch.dto;

import java.time.LocalDateTime;

public class Quote {

    private Long id;
    private String quoteId;
    private String basicQuoteId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String requestText;
    private LocalDateTime createdTime;
    private boolean optIn;

    public Quote() {
    }

    public Quote(Long id, String quoteId, String basicQuoteId, String customerName, String customerEmail, String customerPhone, String requestText, LocalDateTime createdTime, boolean optIn) {
        this.id = id;
        this.quoteId = quoteId;
        this.basicQuoteId = basicQuoteId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.requestText = requestText;
        this.createdTime = createdTime;
        this.optIn = optIn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }

    public String getBasicQuoteId() {
        return basicQuoteId;
    }

    public void setBasicQuoteId(String basicQuoteId) {
        this.basicQuoteId = basicQuoteId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getRequestText() {
        return requestText;
    }

    public void setRequestText(String requestText) {
        this.requestText = requestText;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public boolean isOptIn() {
        return optIn;
    }

    public void setOptIn(boolean optIn) {
        this.optIn = optIn;
    }
}
