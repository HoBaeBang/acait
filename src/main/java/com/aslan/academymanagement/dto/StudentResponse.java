package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.Student;
import com.aslan.academymanagement.domain.enums.Division;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponse {

    private Long id;
    private String studentId;
    private String name;
    private LocalDate birthDate;
    private String phoneNumber;
    private String parentPhoneNumber;
    private com.aslan.academymanagement.domain.enums.Grade grade;
    private Division division;
    private Integer attendanceCount;
    private Double averageScore;
    private String specialNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StudentResponse from(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .studentId(student.getStudentId())
                .name(student.getName())
                .birthDate(student.getBirthDate())
                .phoneNumber(student.getPhoneNumber())
                .parentPhoneNumber(student.getParentPhoneNumber())
                .grade(student.getGrade())
                .division(student.getDivision())
                .attendanceCount(student.getAttendanceCount())
                .averageScore(student.getAverageScore())
                .specialNotes(student.getSpecialNotes())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }
}
