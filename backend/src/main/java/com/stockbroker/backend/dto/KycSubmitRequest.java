package com.stockbroker.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class KycSubmitRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$",
            message = "PAN must be in the format AAAAA9999A")
    private String panNumber;

    @NotBlank(message = "DEMAT id is required")
    private String demateId;

    @NotBlank(message = "Bank account number is required")
    private String bankAccountNumber;

    @NotBlank(message = "Bank IFSC is required")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
            message = "IFSC must be in the format AAAA0999999")
    private String bankIfsc;
}
