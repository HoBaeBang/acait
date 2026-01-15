package com.aslan.academymanagement.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Division {
    ELEMENTARY("초등부"),
    MIDDLE("중등부"),
    HIGH("고등부"),
    NONE("기타");

    private final String description;
}
