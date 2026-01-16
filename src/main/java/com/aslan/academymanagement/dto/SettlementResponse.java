package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.Settlement;
import com.aslan.academymanagement.domain.enums.SettlementStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SettlementResponse {
    private Long id;
    private String instructorName;
    private String yearMonth;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal realAmount;
    private SettlementStatus status;

    public static SettlementResponse from(Settlement settlement) {
        return SettlementResponse.builder()
                .id(settlement.getId())
                .instructorName(settlement.getInstructor().getName())
                .yearMonth(settlement.getYearMonth())
                .totalAmount(settlement.getTotalAmount())
                .taxAmount(settlement.getTaxAmount())
                .realAmount(settlement.getRealAmount())
                .status(settlement.getStatus())
                .build();
    }
}
