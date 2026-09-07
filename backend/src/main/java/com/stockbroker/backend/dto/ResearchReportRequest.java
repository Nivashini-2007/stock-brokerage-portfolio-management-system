package com.stockbroker.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResearchReportRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotBlank(message = "Recommendation is required")
    private String recommendation;

    @NotNull(message = "Target price is required")
    @DecimalMin(value = "0.01", message = "Target price must be greater than 0")
    private Double targetPrice;

    @NotBlank(message = "Summary is required")
    private String summary;
}
