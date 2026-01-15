package com.aslan.academymanagement.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AttendanceStatus {
    ATTENDED("출석"),
    LATE("지각"),
    ABSENT("결석"),
    REQ_MAKEUP("보강 필요"), // 결석했지만 보강을 잡아야 하는 상태
    MAKEUP("보강 수업");     // 보강으로 진행된 수업

    private final String description;
}
