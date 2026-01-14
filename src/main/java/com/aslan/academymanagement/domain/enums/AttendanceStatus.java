package com.aslan.academymanagement.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AttendanceStatus {
    ATTENDED("출석"),
    LATE("지각"),
    ABSENT("결석");

    private final String description;
}
