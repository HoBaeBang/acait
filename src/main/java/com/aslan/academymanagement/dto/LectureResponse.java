package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.Lecture;
import com.aslan.academymanagement.domain.enums.LectureType;
import com.aslan.academymanagement.domain.enums.Subject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureResponse {
    private Long id;
    private String title;
    private LectureType lectureType;
    private Subject subject;
    private List<LectureScheduleResponse> schedules;  // 엔티티 대신 DTO 사용
    private LocalDateTime createdAt;  // createAt → createdAt
    private LocalDateTime updatedAt;  // updateAt → updatedAt

    public static LectureResponse from(Lecture lecture) {
        return LectureResponse.builder()
                .id(lecture.getId())
                .title(lecture.getTitle())
                .lectureType(lecture.getLectureType())
                .subject(lecture.getSubject())
                .schedules(lecture.getSchedules().stream()
                        .map(LectureScheduleResponse::from)  // DTO로 변환
                        .collect(Collectors.toList()))
                .createdAt(lecture.getCreatedAt())  // createAt → createdAt
                .updatedAt(lecture.getUpdatedAt())  // updateAt → updatedAt
                .build();
    }
}
