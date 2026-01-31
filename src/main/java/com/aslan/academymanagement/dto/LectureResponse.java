package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.Lecture;
import com.aslan.academymanagement.domain.enums.LectureType;
import com.aslan.academymanagement.domain.enums.Subject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureResponse {
    private Long id;
    private String name;
    private String instructorName; // 추가: 강사 이름
    private LectureType type;
    private Subject subject;
    private BigDecimal defaultPrice;
    private Integer defaultDuration;
    private Boolean isActive;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<ScheduleResponse> schedules;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LectureResponse from(Lecture lecture) {
        return LectureResponse.builder()
                .id(lecture.getId())
                .name(lecture.getName())
                .instructorName(lecture.getTeacher().getName()) // 매핑
                .type(lecture.getType())
                .subject(lecture.getSubject())
                .defaultPrice(lecture.getDefaultPrice())
                .defaultDuration(lecture.getDefaultDuration())
                .isActive(lecture.getIsActive())
                .startDate(lecture.getStartDate())
                .endDate(lecture.getEndDate())
                .schedules(lecture.getSchedules().stream()
                        .map(ScheduleResponse::from)
                        .collect(Collectors.toList()))
                .createdAt(lecture.getCreatedAt())
                .updatedAt(lecture.getUpdatedAt())
                .build();
    }
}
