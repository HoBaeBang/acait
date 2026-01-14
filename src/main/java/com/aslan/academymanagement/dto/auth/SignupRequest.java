package com.aslan.academymanagement.dto.auth;

import com.aslan.academymanagement.domain.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank(message = "구글 이메일은 필수입니다.")
    @Email
    private String googleEmail;

    @NotNull(message = "역할은 필수입니다.")
    private Role role; // ROLE_ADMIN or ROLE_INSTRUCTOR

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "전화번호는 필수입니다.")
    private String phone;

    @NotBlank(message = "연락용 이메일은 필수입니다.")
    @Email
    private String contactEmail;

    @Schema(description = "학원 이름 (원장 가입 시 필수)")
    private String academyName;

    @Schema(description = "초대 코드 (강사 가입 시 필수)")
    private String inviteCode;
}
