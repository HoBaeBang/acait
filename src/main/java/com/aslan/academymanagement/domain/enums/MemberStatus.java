package com.aslan.academymanagement.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberStatus {
    PENDING("PENDING", "승인 대기"),
    ACTIVE("ACTIVE", "활동 중"),
    REJECTED("REJECTED", "승인 거절");

    private final String key;
    private final String title;
}
