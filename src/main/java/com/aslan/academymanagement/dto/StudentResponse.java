package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.domain.enums.Division;
import com.aslan.academymanagement.domain.enums.Grade;
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
    private String studentNumber; // studentId -> studentNumber
    private String name;
    private String school; // 추가
    private Grade grade;
    private String birthDate; // LocalDate -> String
    private String parentPhone; // parentPhoneNumber -> parentPhone
    private String parentEmail; // 추가
    private String memo; // specialNotes -> memo
    private Division division;
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
                .division(student.getDivision())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }
}
