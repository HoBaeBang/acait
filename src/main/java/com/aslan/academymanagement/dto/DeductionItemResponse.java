package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.DeductionItem;
import com.aslan.academymanagement.domain.enums.DeductionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DeductionItemResponse {
    private Long id;
    private String name;
    private DeductionType type;
    private BigDecimal value;

    public static DeductionItemResponse from(DeductionItem item) {
        return DeductionItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .type(item.getType())
                .value(item.getValue())
                .build();
    }
}
