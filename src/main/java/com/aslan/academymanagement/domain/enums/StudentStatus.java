package com.aslan.academymanagement.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StudentStatus {
    ATTENDING("재원"),
    DISCHARGED("퇴원");

    private final String description;
}
