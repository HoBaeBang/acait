package com.aslan.academymanagement.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LectureType {
    ACADEMY("학원 정규 강의"),
    TUTORING("1:1 과외"),
    SPECIAL_LECTURE("특강");

    private final String description;
}
