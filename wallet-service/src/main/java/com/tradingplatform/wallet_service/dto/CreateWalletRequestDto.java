package com.tradingplatform.wallet_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletRequestDto {

    @NotNull(message = "User ID is required")
    private Long userId;

    private String role = "BASIC";
}
