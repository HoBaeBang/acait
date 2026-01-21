package com.aslan.academymanagement.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SettlementForecastResponse {
    private BigDecimal confirmedAmount; // 확정 금액 (지난 수업)
    private BigDecimal expectedAmount;  // 예정 금액 (남은 수업)
    private BigDecimal totalForecast;   // 총 예상 금액
}
