package com.stockbroker.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KycReviewRequest {

    @NotNull(message = "Approved flag is required")
    private Boolean approved;

    private String remarks;
}
