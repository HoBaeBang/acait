package com.aslan.academymanagement.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Grade {
    // 초등부
    E1(Division.ELEMENTARY, "초1"), E2(Division.ELEMENTARY, "초2"), E3(Division.ELEMENTARY, "초3"),
    E4(Division.ELEMENTARY, "초4"), E5(Division.ELEMENTARY, "초5"), E6(Division.ELEMENTARY, "초6"),

    // 중등부
    M1(Division.MIDDLE, "중1"), M2(Division.MIDDLE, "중2"), M3(Division.MIDDLE, "중3"),

    // 고등부
    H1(Division.HIGH, "고1"), H2(Division.HIGH, "고2"), H3(Division.HIGH, "고3"),

    // 기타
    N(Division.NONE, "기타");

    private final Division division;
    private final String description;
}
