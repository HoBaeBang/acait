package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.Lecture;
import com.aslan.academymanagement.domain.LectureSchedule;
import com.aslan.academymanagement.domain.enums.LectureType;
import com.aslan.academymanagement.domain.enums.Subject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor // JSON 파싱을 위해 기본 생성자 필요
@AllArgsConstructor
public class LectureRequest {
    private String title;
    private LectureType lectureType;
    private Subject subject;
    private List<LectureScheduleRequest> scheduleRequest;

    public Lecture toLecture() {
        return Lecture.builder()
                .title(title)
                .lectureType(lectureType)
                .subject(subject)
                .build();
    }

    public List<LectureSchedule> toLectureSchedules() {
        if (scheduleRequest == null) {
            return List.of();
        }
        return scheduleRequest.stream()
                .map((t) -> LectureSchedule.builder()
                        .dayOfWeek(t.dayOfWeek())
                        .startTime(t.startTime())
                        .endTime(t.endTime())
                        .build())
                .toList();
    }
}
