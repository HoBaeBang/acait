package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.Academy;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AcademyResponse {
    private Long id;
    private String name;
    private String inviteCode;

    public static AcademyResponse from(Academy academy) {
        return AcademyResponse.builder()
                .id(academy.getId())
                .name(academy.getName())
                .inviteCode(academy.getInviteCode())
                .build();
    }
}
