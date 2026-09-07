package com.stockbroker.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class TaxReportResponse {

    private Long clientId;
    private Integer year;

    private Double totalInvestment;
    private Double totalCurrentValue;
    private Double unrealizedProfit;

    private Double shortTermGain;
    private Double longTermGain;
    private Double totalRealizedProfit;

    /** FIFO cost-basis, scrip-wise breakdown of every closed lot this year. */
    private List<RealizedGainResponse> realizedGains;

    /** Configurable LTCG exemption (default ₹125,000/FY) already netted out. */
    private Double ltcgExemptionApplied;
    private Double ltcgTaxableAmount;
    private Double stcgTaxableAmount;

    private Double estimatedLtcgTax;
    private Double estimatedStcgTax;
    private Double totalEstimatedTax;
}
