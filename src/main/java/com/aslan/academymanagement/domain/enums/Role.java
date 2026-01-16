package com.aslan.academymanagement.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    ROLE_OWNER("ROLE_OWNER", "원장"), // ADMIN -> OWNER 변경
    ROLE_INSTRUCTOR("ROLE_INSTRUCTOR", "강사"),
    ROLE_GUEST("ROLE_GUEST", "손님");

    private final String key;
    private final String title;
}
