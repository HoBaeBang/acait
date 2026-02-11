package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.StudentBalance;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class StudentBalanceResponse {
    private String yearMonth;
    private BigDecimal paidAmount;
    private BigDecimal usedAmount;
    private BigDecimal carryOverAmount;
    private BigDecimal currentBalance;

    public static StudentBalanceResponse from(StudentBalance balance) {
        return StudentBalanceResponse.builder()
                .yearMonth(balance.getYearMonth())
                .paidAmount(balance.getPaidAmount())
                .usedAmount(balance.getUsedAmount())
                .carryOverAmount(balance.getCarryOverAmount())
                .currentBalance(balance.getCurrentBalance())
                .build();
    }
}
