package com.aslan.academymanagement.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AttendanceStatus {
    ATTENDED("출석"),
    LATE("지각"),
    ABSENT("결석"), // 당일 통보 결석 (정산 O, 보강 X)
    REQ_MAKEUP("보강 필요"), // 사전 협의 결석 (정산 X, 보강 O)
    MAKEUP("보강 수업"),     // 보강으로 진행된 수업
    CANCELLED("휴강");       // 수업 취소 (정산 X, 보강 X)

    private final String description;
}
