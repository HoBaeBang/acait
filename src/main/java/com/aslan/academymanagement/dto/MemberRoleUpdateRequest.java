package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.enums.Role;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class MemberRoleUpdateRequest {
    @NotNull(message = "변경할 역할은 필수입니다.")
    private Role role;
}
