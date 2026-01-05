package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.enums.Division;
import com.aslan.academymanagement.domain.enums.Grade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRequest {

    @NotBlank(message = "학생 번호는 필수입니다")
    @Pattern(regexp = "^(ES|MS)\\d{3}$", message = "학생 번호 형식: ES001 또는 MS001")
    private String studentId;

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @NotNull(message = "생년월일은 필수입니다")
    private LocalDate birthDate;

    private String phoneNumber;

    private String parentPhoneNumber;

    @NotNull(message = "학년은 필수입니다")
    private Grade grade;

    @NotNull(message = "부서는 필수입니다")
    private Division division;

    private String specialNotes;
}
