package com.tradingplatform.user_service.dto;

import com.tradingplatform.user_service.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
