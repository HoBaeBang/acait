package com.aslan.academymanagement.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeductionType {
    PERCENT("비율(%)"),
    FIXED_AMOUNT("고정 금액(원)");

    private final String description;
}
