package com.tradingplatform.order_service.dto;

import com.tradingplatform.order_service.model.OrderType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequestDto {

    @NotNull(message = "User Id is required")
    private Long userId;

    @NotBlank(message = "Symbol is required")
    @Size(min = 1, max = 10, message = "Symbol must be between 1 and 10 characters")
    private String symbol;

    @NotNull(message = "Order type is required")
    private OrderType orderType;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10000, message = "Quantity cannot exceed 10,000")
    private Integer quantity;

    private String notes;
}
