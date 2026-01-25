package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.domain.enums.Grade;
import com.aslan.academymanagement.domain.enums.StudentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    // studentNumber는 자동 생성되므로 요청에서 제외

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    private String school;

    @NotNull(message = "학년은 필수입니다")
    private Grade grade;

    @NotBlank(message = "생일(4자리)은 필수입니다")
    private String birthDate;

    @NotBlank(message = "학부모 연락처는 필수입니다")
    private String parentPhone;

    private String parentEmail;

    private String memo;

    // 상태 변경을 위한 필드 추가
    private StudentStatus status;
    private LocalDate dischargeDate;

    public Student toEntity() {
        return Student.builder()
                .name(name)
                .school(school)
                .grade(grade)
                .birthDate(birthDate)
                .parentPhone(parentPhone)
                .parentEmail(parentEmail)
                .memo(memo)
                .status(status) // 추가
                .dischargeDate(dischargeDate) // 추가
                .build();
    }
}
