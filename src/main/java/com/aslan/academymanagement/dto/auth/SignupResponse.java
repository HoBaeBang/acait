package com.aslan.academymanagement.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse {
    private String message;
    private String token; // JWT 토큰 (ACTIVE 상태일 때만 발급)
}
