package com.aslan.academymanagement.domain.enums;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
public enum Subject {
    KOREAN("국어"),
    MATH("수학"),
    ENGLISH("영어"),
    SCIENCE("과학"),
    SOCIAL("사회"),
    KOREAN_HISTORY("한국사"),
    ;

    private final String value;
}
