package com.digitalq.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyTokenRequest {
    @NotBlank(message = "Token number is required")
    private String tokenNumber;
}
