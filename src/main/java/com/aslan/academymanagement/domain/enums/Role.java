package com.aslan.academymanagement.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    GUEST("ROLE_GUEST", "손님"),       // 최초 가입 시 (정보 미입력)
    STUDENT("ROLE_STUDENT", "학생"),
    TEACHER("ROLE_TEACHER", "선생님"),
    ADMIN("ROLE_ADMIN", "관리자");

    private final String key;
    private final String title;
}
