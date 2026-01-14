package com.aslan.academymanagement.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LectureType {
    BOARD("판서 수업 (고정)"),
    INDIV("개별 진도 (유동)");

    private final String description;
}
