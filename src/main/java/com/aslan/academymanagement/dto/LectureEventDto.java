package com.aslan.academymanagement.dto;

import com.aslan.academymanagement.domain.Lecture;
import com.aslan.academymanagement.domain.Schedule;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class LectureEventDto {
    private String id; // Schedule ID
    private String title;
    private String start;
    private String end;
    private String color;
    private Map<String, Object> extendedProps;

    // 기간(start~end) 내의 반복 일정을 계산하여 이벤트 리스트 반환
    public static List<LectureEventDto> from(Lecture lecture, LocalDate queryStart, LocalDate queryEnd) {
        List<LectureEventDto> events = new ArrayList<>();

        // 강의 기간과 조회 기간의 교집합 구하기
        LocalDate effectiveStart = lecture.getStartDate().isAfter(queryStart) ? lecture.getStartDate() : queryStart;
        LocalDate effectiveEnd = lecture.getEndDate().isBefore(queryEnd) ? lecture.getEndDate() : queryEnd;

        if (effectiveStart.isAfter(effectiveEnd)) {
            return events; // 겹치는 기간 없음
        }

        for (Schedule schedule : lecture.getSchedules()) {
            // effectiveStart부터 effectiveEnd까지 하루씩 증가하며 요일 체크
            for (LocalDate date = effectiveStart; !date.isAfter(effectiveEnd); date = date.plusDays(1)) {
                if (date.getDayOfWeek() == schedule.getDayOfWeek()) {
                    
                    LocalDateTime startDateTime = LocalDateTime.of(date, schedule.getStartTime());
                    LocalDateTime endDateTime = LocalDateTime.of(date, schedule.getEndTime());

                    String subjectName = (lecture.getSubject() != null) ? lecture.getSubject().name() : "NONE";

                    Map<String, Object> props = new HashMap<>();
                    props.put("instructor", lecture.getTeacher().getName());
                    props.put("lectureId", lecture.getId());

                    events.add(LectureEventDto.builder()
                            .id(String.valueOf(schedule.getId()))
                            .title(lecture.getName())
                            .start(startDateTime.toString())
                            .end(endDateTime.toString())
                            .color(getColorBySubject(subjectName))
                            .extendedProps(props)
                            .build());
                }
            }
        }
        return events;
    }

    private static String getColorBySubject(String subject) {
        if (subject == null) return "#6c757d";

        switch (subject) {
            case "KOREAN": return "#dc3545";
            case "ENGLISH": return "#28a745";
            case "MATH": return "#007bff";
            case "SCIENCE": return "#ffc107";
            default: return "#6c757d";
        }
    }
}
