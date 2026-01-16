package com.aslan.academymanagement.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SettlementStatus {
    OPEN("정산 예정"),
    CLOSED("정산 마감");

    private final String description;
}
