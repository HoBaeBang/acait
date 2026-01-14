package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.domain.enums.Division;
import com.aslan.academymanagement.domain.enums.Grade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRequest {

    @NotBlank(message = "학생 번호는 필수입니다")
    private String studentNumber;

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

    @NotNull(message = "부서는 필수입니다")
    private Division division;

    public Student toEntity() {
        return Student.builder()
                .studentNumber(studentNumber)
                .name(name)
                .school(school)
                .grade(grade)
                .birthDate(birthDate)
                .parentPhone(parentPhone)
                .parentEmail(parentEmail)
                .memo(memo)
                .division(division)
                .build();
    }
}
