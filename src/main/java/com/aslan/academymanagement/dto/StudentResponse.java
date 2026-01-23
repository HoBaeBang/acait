package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.domain.enums.Division;
import com.aslan.academymanagement.domain.enums.Grade;
import com.aslan.academymanagement.domain.enums.StudentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponse {

    private Long id;
    private String studentNumber;
    private String name;
    private String school;
    private Grade grade;
    private String birthDate;
    private String parentPhone;
    private String parentEmail;
    private String memo;
    private Division division; // Grade에서 추출
    private StudentStatus status; // 추가됨
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StudentResponse from(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .studentNumber(student.getStudentNumber())
                .name(student.getName())
                .school(student.getSchool())
                .grade(student.getGrade())
                .birthDate(student.getBirthDate())
                .parentPhone(student.getParentPhone())
                .parentEmail(student.getParentEmail())
                .memo(student.getMemo())
                .division(student.getGrade().getDivision()) // Grade에서 Division 추출
                .status(student.getStatus()) // 추가됨
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }
}
