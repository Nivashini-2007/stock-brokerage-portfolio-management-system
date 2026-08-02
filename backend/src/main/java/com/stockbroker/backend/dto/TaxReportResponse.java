package com.stockbroker.backend.dto;

public class TaxReportResponse {

    private Long clientId;
    private Integer year;

    private Double totalInvestment;
    private Double totalCurrentValue;

    private Double realizedProfit;
    private Double unrealizedProfit;

    private Double totalProfit;
    private Double taxableProfit;
    private Double estimatedTax;

    public TaxReportResponse() {
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Double getTotalInvestment() {
        return totalInvestment;
    }

    public void setTotalInvestment(Double totalInvestment) {
        this.totalInvestment = totalInvestment;
    }

    public Double getTotalCurrentValue() {
        return totalCurrentValue;
    }

    public void setTotalCurrentValue(Double totalCurrentValue) {
        this.totalCurrentValue = totalCurrentValue;
    }

    public Double getRealizedProfit() {
        return realizedProfit;
    }

    public void setRealizedProfit(Double realizedProfit) {
        this.realizedProfit = realizedProfit;
    }

    public Double getUnrealizedProfit() {
        return unrealizedProfit;
    }

    public void setUnrealizedProfit(Double unrealizedProfit) {
        this.unrealizedProfit = unrealizedProfit;
    }

    public Double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(Double totalProfit) {
        this.totalProfit = totalProfit;
    }

    public Double getTaxableProfit() {
        return taxableProfit;
    }

    public void setTaxableProfit(Double taxableProfit) {
        this.taxableProfit = taxableProfit;
    }

    public Double getEstimatedTax() {
        return estimatedTax;
    }

    public void setEstimatedTax(Double estimatedTax) {
        this.estimatedTax = estimatedTax;
    }
}