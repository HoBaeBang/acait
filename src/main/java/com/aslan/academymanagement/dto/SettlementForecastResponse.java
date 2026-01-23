package com.aslan.academymanagement.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SettlementForecastResponse {
    private BigDecimal confirmedAmount; // 확정 금액 (세전)
    private BigDecimal expectedAmount;  // 예정 금액 (세전)

    private BigDecimal totalAmount;     // 총 예상 금액 (세전)
    private BigDecimal taxAmount;       // 예상 세금 (3.3%)
    private BigDecimal realAmount;      // 예상 실수령액 (세후)
}
