package com.digitalq.dto;

import com.digitalq.model.Token;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TokenResponse {
    private Long id;
    private String tokenNumber;
    private String customerName;
    private Long counterId;
    private String counterName;
    private String status;
    private Integer position;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String qrCodeBase64;

    public static TokenResponse fromToken(Token token, Integer position, String qrCodeBase64) {
        return TokenResponse.builder()
                .id(token.getId())
                .tokenNumber(token.getTokenNumber())
                .customerName(token.getCustomerName())
                .counterId(token.getCounterId())
                .status(token.getStatus().name())
                .position(position)
                .createdAt(token.getCreatedAt())
                .expiresAt(token.getExpiresAt())
                .qrCodeBase64(qrCodeBase64)
                .build();
    }
}
